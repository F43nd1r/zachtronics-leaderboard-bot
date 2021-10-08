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

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.discord4j.command.annotation.Description;
import com.faendir.zachtronics.bot.discord.command.AbstractShowCommand;
import com.faendir.zachtronics.bot.leaderboards.Leaderboard;
import com.faendir.zachtronics.bot.sc.ScQualifier;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import kotlin.Pair;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@ScQualifier
public class ScShowCommand extends AbstractShowCommand<ScCategory, ScPuzzle, ScRecord> {
    @Getter
    private final List<Leaderboard<ScCategory, ScPuzzle, ScRecord>> leaderboards;

    @NotNull
    @Override
    public Pair<ScPuzzle, ScCategory> findPuzzleAndCategory(@NotNull ChatInputInteractionEvent interaction) {
        Data data = ScShowCommand$DataParser.parse(interaction);
        return new Pair<>(data.puzzle, data.category);
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScShowCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "show", description = "Show a record", subCommand = true)
    @Value
    public static class Data {
        @NonNull ScPuzzle puzzle;
        @NonNull ScCategory category;

        public Data(@Description("Puzzle name. Can be shortened or abbreviated. E.g. `sus beha`, `OPAS`")
                    @Converter(ScPuzzleConverter.class) @NonNull ScPuzzle puzzle,
                    @Description("Category. E.g. `C`, `RSNB`")
                    @NonNull ScCategory category) {
            this.puzzle = puzzle;
            this.category = category;
        }
    }
}
