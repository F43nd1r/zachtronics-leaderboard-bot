/*
 * Copyright (c) 2022
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

package com.faendir.zachtronics.bot.discord;

import com.faendir.discord4j.command.parse.OptionConverter;
import com.faendir.discord4j.command.parse.SingleParseResult;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListConverter implements OptionConverter<String, List<String>> {
    private static final String SEPARATOR = ",";

    @NotNull
    @Override
    public SingleParseResult<List<String>> fromValue(@NotNull ChatInputInteractionEvent context,
                                                     @NotNull String value) {
        String[] values = value.split(SEPARATOR);
        if (values.length == 0)
            return new SingleParseResult.Failure<>("No value found");
        else
            return new SingleParseResult.Success<>(List.of(values));
    }
}
