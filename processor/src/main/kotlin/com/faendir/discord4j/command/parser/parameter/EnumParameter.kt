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

import com.faendir.discord4j.command.annotation.Name
import com.faendir.discord4j.command.parse.SingleParseResult
import com.faendir.discord4j.command.parser.findAnnotationProperty
import com.faendir.discord4j.command.parser.nonnull
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.CodeBlock
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData
import io.github.enjoydambience.kotlinbard.CodeBlockBuilder
import io.github.enjoydambience.kotlinbard.codeBlock
import net.pearx.kasechange.toKebabCase

class EnumParameter(parameter: KSValueParameter, index: Int) : Parameter(parameter, index) {
    override val optionType: ApplicationCommandOption.Type = ApplicationCommandOption.Type.STRING
    private val typeDeclaration = type.declaration as KSClassDeclaration

    override fun CodeBlockBuilder.modifyDataBuilder() {
        for (enumValue in typeDeclaration.declarations.filterIsInstance<KSClassDeclaration>().filter { it.classKind == ClassKind.ENUM_ENTRY }) {
            val value = enumValue.simpleName.asString()
            add(
                ".addChoice(%T.builder().name(%S).value(%S).build())\n",
                ApplicationCommandOptionChoiceData::class,
                enumValue.findAnnotationProperty(Name::value) ?: value.toKebabCase(),
                value
            )
        }
    }

    override fun convertValue(): CodeBlock =
        codeBlock(
            if (isRequired) "asString().let·{ %1T(%2T.valueOf(it)) }" else "asString()?.let·{ %2T.valueOf(it) }.let·{ %1T(it) }",
            SingleParseResult.Success::class,
            typeName.nonnull
        )
}