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

import com.faendir.discord4j.command.parse.SingleParseResult
import com.faendir.discord4j.command.parser.asClassName
import com.faendir.discord4j.command.parser.decapitalizedSimpleName
import com.faendir.discord4j.command.parser.mapToKotlin
import com.faendir.discord4j.command.parser.simpleName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.CodeBlock
import discord4j.core.`object`.command.ApplicationCommandOption
import io.github.enjoydambience.kotlinbard.codeBlock

class CustomParameter(
    parameter: KSValueParameter,
    index: Int,
    private val converter: KSType,
    private val inputType: KSType
) : Parameter(parameter, index) {
    override val optionType: ApplicationCommandOption.Type =
        if (inputType.asClassName().mapToKotlin() == BOOLEAN) ApplicationCommandOption.Type.BOOLEAN else ApplicationCommandOption.Type.STRING
    override val dependencies = super.dependencies + converter
    override fun convertValue(): CodeBlock =
        codeBlock {
            add("let·{·value·-> %L.fromValue(event, value.as%L()) }", converter.decapitalizedSimpleName, inputType.simpleName)
            if (!isRequired)
                add(" ?: %T(null)", SingleParseResult.Success::class)
        }
}