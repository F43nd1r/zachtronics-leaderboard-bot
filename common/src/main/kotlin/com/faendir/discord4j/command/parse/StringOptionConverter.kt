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

package com.faendir.discord4j.command.parse

import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent

interface StringOptionConverter<T>: OptionConverter<T> {
    override fun fromValue(
        context: ChatInputInteractionEvent,
        value: ApplicationCommandInteractionOptionValue
    ): SingleParseResult<T> = fromString(context, value.asString())
    fun fromString(context: ChatInputInteractionEvent, string: String): SingleParseResult<T>
}