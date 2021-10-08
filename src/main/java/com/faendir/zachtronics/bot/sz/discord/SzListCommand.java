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

package com.faendir.zachtronics.bot.sz.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.zachtronics.bot.discord.command.AbstractListCommand;
import com.faendir.zachtronics.bot.leaderboards.Leaderboard;
import com.faendir.zachtronics.bot.sz.SzQualifier;
import com.faendir.zachtronics.bot.sz.model.SzCategory;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzRecord;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import kotlin.Pair;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@SzQualifier
public class SzListCommand extends AbstractListCommand<SzCategory, SzPuzzle, SzRecord> {
    @Getter
    private final List<Leaderboard<SzCategory, SzPuzzle, SzRecord>> leaderboards;

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return SzListCommand$DataParser.buildData();
    }

    @NotNull
    @Override
    public Pair<SzPuzzle, List<SzCategory>> findPuzzleAndCategories(@NotNull ChatInputInteractionEvent interaction) {
        Data data = SzListCommand$DataParser.parse(interaction);
        return new Pair<>(data.puzzle, Arrays.stream(SzCategory.values())
                .filter(c -> c.supportsPuzzle(data.puzzle))
                .collect(Collectors.toList()));
    }

    @ApplicationCommand(name = "list", subCommand = true)
    @Value
    public static class Data {
        @NonNull SzPuzzle puzzle;

        public Data(@Converter(SzPuzzleConverter.class) @NonNull SzPuzzle puzzle) {
            this.puzzle = puzzle;
        }
    }
}
