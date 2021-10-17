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

import com.faendir.discord4j.command.annotation.Description
import com.faendir.discord4j.command.annotation.Name
import com.faendir.discord4j.command.annotation.Required
import com.faendir.discord4j.command.parser.asTypeName
import com.faendir.discord4j.command.parser.findAnnotationProperty
import com.faendir.discord4j.command.parser.hasAnnotation
import com.faendir.discord4j.command.parser.hasAnnotationWithName
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Origin
import com.squareup.kotlinpoet.CodeBlock
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.discordjson.json.ApplicationCommandOptionData
import io.github.enjoydambience.kotlinbard.CodeBlockBuilder
import io.github.enjoydambience.kotlinbard.codeBlock
import net.pearx.kasechange.toKebabCase

abstract class Parameter(private val parameter: KSValueParameter, private val index: Int) {
    val type = parameter.type.resolve()
    val isRequired: Boolean = when (parameter.origin) {
        Origin.KOTLIN, Origin.KOTLIN_LIB, Origin.SYNTHETIC -> !type.isMarkedNullable || parameter.hasAnnotation<Required>()
        Origin.JAVA, Origin.JAVA_LIB -> type.nullability == Nullability.NOT_NULL || parameter.hasAnnotationWithName(
            "Nonnull",
            "NonNull",
            "NotNull",
            "Required"
        ) && !parameter.hasAnnotationWithName("Nullable")
        else -> true
    }
    val typeName = type.asTypeName().copy(nullable = !isRequired)
    val name = parameter.findAnnotationProperty(Name::value) ?: (parameter.name?.asString() ?: "var$index").toKebabCase()

    abstract val optionType: ApplicationCommandOption.Type

    fun buildData(): CodeBlock = codeBlock {
        add("%T.builder()\n", ApplicationCommandOptionData::class)
        indent()
        add(".name(%S)\n", name)
        add(".description(%S)\n", parameter.findAnnotationProperty(Description::value) ?: name)
        if (isRequired) {
            add(".required(true)\n")
        }
        add(".type(%T.Type.%L.value)\n", ApplicationCommandOption::class, optionType.name)
        modifyDataBuilder()
        add(".build()")
        unindent()
    }

    open fun CodeBlockBuilder.modifyDataBuilder() {
    }

    abstract fun convertValue(): CodeBlock

    fun toParseResult(): CodeBlock = codeBlock {
        add(
            if (isRequired) "options.first·{ %S·== it.name }.value.get().%L"
            else "options.firstOrNull·{ %S·== it.name }?.value?.orElse(null)?.%L",
            (parameter.name?.asString() ?: "var$index").toKebabCase(),
            convertValue()
        )
    }
}