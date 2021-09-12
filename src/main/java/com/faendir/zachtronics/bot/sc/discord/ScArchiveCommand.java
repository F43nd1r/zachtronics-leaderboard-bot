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
import com.faendir.zachtronics.bot.discord.LinkConverter;
import com.faendir.zachtronics.bot.discord.command.AbstractArchiveCommand;
import com.faendir.zachtronics.bot.sc.ScQualifier;
import com.faendir.zachtronics.bot.sc.archive.ScArchive;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@ScQualifier
public class ScArchiveCommand extends AbstractArchiveCommand<ScSolution> implements ScSecured {
    @Getter
    private final ScArchive archive;

    @NotNull
    @Override
    public List<ScSolution> parseSolutions(@NotNull SlashCommandEvent interaction) {
        Data data = ScArchiveCommand$DataParser.parse(interaction);
        return ScSolution.fromExportLink(data.export, data.puzzle);
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScArchiveCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "archive", description = "Archive any number of solutions in an export file", subCommand = true)
    @Value
    public static class Data {
        @NotNull String export;
        ScPuzzle puzzle;

        public Data(@NotNull
                    @Description("Link or `m1` to scrape it from your last message. " +
                                 "Start the solution name with `/B?P?` to set flags")
                    @Converter(LinkConverter.class) String export,
                    @Description("Puzzle name. Can be shortened or abbreviated. E.g. `sus beha`, `OPAS`")
                    @Converter(ScPuzzleConverter.class) ScPuzzle puzzle) {
            this.export = export;
            this.puzzle = puzzle;
        }
    }
}
