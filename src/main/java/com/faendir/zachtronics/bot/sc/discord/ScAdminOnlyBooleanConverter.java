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

package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.parse.OptionConverter;
import com.faendir.discord4j.command.parse.SingleParseResult;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import org.jetbrains.annotations.NotNull;

public class ScAdminOnlyBooleanConverter implements OptionConverter<Boolean, Boolean> {
    @NotNull
    @Override
    public SingleParseResult<Boolean> fromValue(@NotNull ChatInputInteractionEvent context,
                                                @NotNull Boolean value) {
        if (value && !ScSecured.WIKI_ADMINS_ONLY.hasExecutionPermission(UtilsKt.user(context)))
            return new SingleParseResult.Failure<>("Only a wiki admin can use this parameter");
        else
            return new SingleParseResult.Success<>(value);
    }
}
