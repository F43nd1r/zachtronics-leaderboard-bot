/*
 * Copyright (c) 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.discord4j.command.parser

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.ApplicationCommandConstructor
import com.faendir.discord4j.command.parse.ApplicationCommandParser
import com.faendir.discord4j.command.parse.CombinedParseResult
import com.faendir.discord4j.command.parse.SingleParseResult
import com.faendir.discord4j.command.parser.parameter.ParameterFactory
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.asClassName
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import io.github.enjoydambience.kotlinbard.`if`
import io.github.enjoydambience.kotlinbard.`while`
import io.github.enjoydambience.kotlinbard.addCode
import io.github.enjoydambience.kotlinbard.addFunction
import io.github.enjoydambience.kotlinbard.addObject
import io.github.enjoydambience.kotlinbard.buildFile
import io.github.enjoydambience.kotlinbard.nullable
import net.pearx.kasechange.toKebabCase

class Generator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val resolver: Resolver
) {

    private fun error(reason: String, clazz: KSClassDeclaration) {
        logger.error("@AutoDsl can't be applied to $clazz: $reason", clazz)
    }


    fun process(): List<KSAnnotated> {
        return resolver.getClassesWithAnnotation(ApplicationCommand::class.java.name).filterNot { generate(it) }.toList()
    }


    /**
     * returns true if class generation was successful
     */
    private fun generate(clazz: KSClassDeclaration): Boolean {
        if (clazz.isAbstract()) {
            error("must not be abstract", clazz)
            return false
        }
        val constructors = clazz.getConstructors().filter { it.isPublic() || it.isInternal() }.toList()
        if (constructors.isEmpty()) {
            error("must have at least one public or internal constructor", clazz)
            return false
        }
        val constructor = constructors.firstOrNull { it.hasAnnotation<ApplicationCommandConstructor>() }
            ?: clazz.primaryConstructor
            ?: constructors.first()
        if (!constructor.validate()) {
            //defer processing
            return false
        }
        val parameters = ParameterFactory.createParameters(constructor.parameters, logger)
        val type = clazz.asStarProjectedType().asClassName()
        val subCommand = clazz.findAnnotationProperty(ApplicationCommand::subCommand) ?: false
        val dataType = if (subCommand) ApplicationCommandOptionData::class else ApplicationCommandRequest::class
        val parserType = type.withParserSuffix()
        buildFile(parserType.packageName, parserType.simpleName) {
            addObject(parserType) {
                addSuperinterface(ApplicationCommandParser::class.asClassName().parameterizedBy(type, dataType.asClassName()))
                addFunction("buildData") {
                    addModifiers(KModifier.OVERRIDE)
                    returns(dataType)
                    addCode {
                        add("return %T.builder()\n", dataType)
                        indent()
                        val name = clazz.findAnnotationProperty(ApplicationCommand::name)?.takeIf { it.isNotEmpty() } ?: type.simpleName.toKebabCase()
                        add(".name(%S)\n", name)
                        add(".description(%S)\n", clazz.findAnnotationProperty(ApplicationCommand::description)?.takeIf { it.isNotEmpty() } ?: name)
                        if (subCommand) add(".type(%T.Type.SUB_COMMAND.value)\n", ApplicationCommandOption::class)
                        for (parameter in parameters.sortedByDescending { it.isRequired }) {
                            add(".addOption(%L)\n", parameter.buildData())
                        }
                        add(".build()")
                        unindent()
                    }
                }
                addFunction("map") {
                    addModifiers(KModifier.OVERRIDE)
                    returns(type.nullable)
                    addParameter("parameters", MAP.parameterizedBy(STRING, ANY.nullable))
                    addCode {
                        `if`("parameters.size == %L", parameters.size) {
                            add("return %T(\n", type)
                            indent()
                            for (parameter in parameters) {
                                add("parameters[%S] as %T,\n", parameter.name, parameter.typeName)
                            }
                            unindent()
                            add(")\n")
                        }.`else` {
                            addStatement("return null")
                        }
                    }
                }
                addFunction("parse") {
                    addModifiers(KModifier.OVERRIDE)
                    returns(CombinedParseResult::class.asClassName().parameterizedBy(type))
                    addParameter("event", ChatInputInteractionEvent::class)
                    addCode {
                        if (parameters.isEmpty()) {
                            add("return %T(%T())", CombinedParseResult.Success::class, type)
                        } else {
                            addStatement("var options = event.options")
                            addStatement("var option = options.first()")
                            `while`("option.type·== %1T.Type.SUB_COMMAND || option.type·== %1T.Type.SUB_COMMAND_GROUP", ApplicationCommandOption::class) {
                                addStatement("options = option.options")
                                addStatement("option = options.first()")
                            }
                            addStatement("val results = mutableMapOf<%T, %T>()", String::class, SingleParseResult::class.asClassName().parameterizedBy(STAR))
                            for (parameter in parameters) {
                                addStatement("results.put(%S, %L)", parameter.name, parameter.toParseResult())
                            }
                            add("return %T.combine(results)·{ map(it)!! }", CombinedParseResult::class)
                        }
                    }
                }
            }
        }.writeTo(clazz.containingFile!!, codeGenerator)
        return true
    }

}