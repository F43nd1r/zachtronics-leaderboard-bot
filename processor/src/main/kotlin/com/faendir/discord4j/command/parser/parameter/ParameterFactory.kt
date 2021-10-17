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

package com.faendir.discord4j.command.parser.parameter

import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.parser.asTypeName
import com.faendir.discord4j.command.parser.findAnnotationTypeProperty
import com.faendir.discord4j.command.parser.nonnull
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.STRING

object ParameterFactory {

    fun createParameters(params: List<KSValueParameter>, logger: KSPLogger) : List<Parameter> {
        return params.withIndex().map { (index, parameter) ->
            val type = parameter.type.resolve()
            val converter = parameter.findAnnotationTypeProperty(Converter::value)
            when {
                converter != null -> {
                    val inputType = parameter.findAnnotationTypeProperty(Converter::input)!!
                    CustomParameter(parameter, index, converter, inputType)
                }
                (type.declaration as KSClassDeclaration).classKind == ClassKind.ENUM_CLASS -> EnumParameter(parameter, index)
                else -> when(type.asTypeName().nonnull) {
                    INT -> IntParameter(parameter, index)
                    BOOLEAN -> BooleanParameter(parameter, index)
                    STRING -> StringParameter(parameter, index)
                    else -> {
                        logger.error("Custom type must be annotated with @${Converter::class.java.simpleName}", parameter)
                        throw IllegalArgumentException()
                    }
                }
            }
        }
    }
}