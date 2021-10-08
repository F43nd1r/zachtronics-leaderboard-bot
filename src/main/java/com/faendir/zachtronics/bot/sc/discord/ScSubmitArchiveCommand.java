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
import com.faendir.zachtronics.bot.discord.command.AbstractSubmitArchiveCommand;
import com.faendir.zachtronics.bot.sc.ScQualifier;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import kotlin.Triple;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ScQualifier
public class ScSubmitArchiveCommand extends AbstractSubmitArchiveCommand<ScPuzzle, ScRecord, ScSolution> implements ScSecured {
    @Getter
    private final ScSubmitCommand submitCommand;
    @Getter
    private final ScArchiveCommand archiveCommand;

    @NotNull
    @Override
    public Triple<ScPuzzle, ScRecord, ScSolution> parseToPRS(@NotNull ChatInputInteractionEvent event) {
        Data data = ScSubmitArchiveCommand$DataParser.parse(event);
        if (data.export.equals(data.video))
            throw new IllegalArgumentException("Export link and video link cannot be the same link");
        ScSolution solution = archiveCommand.parseSolutions(event).get(0);
        String archiveLink = archiveCommand.getArchive().makeArchiveLink(solution);
        ScRecord record = new ScRecord(solution.getScore(), data.author, data.video, archiveLink, false);
        return new Triple<>(solution.getPuzzle(), record, solution);
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScSubmitArchiveCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "submit-archive", description = "Submit and archive a solution", subCommand = true)
    @Value
    public static class Data {
        @NotNull String video;
        @NotNull String export;
        @NotNull String author;
        ScPuzzle puzzle;
        String bypassValidation;

        public Data(@Description("Link to your video of the solution, can be `m1` to scrape it from your last message")
                    @NotNull @Converter(LinkConverter.class) String video,
                    @Description("Link or `m1` to scrape it from your last message. " +
                                 "Start the solution name with `/B?P?` to set flags")
                    @NotNull @Converter(LinkConverter.class) String export,
                    @Description("Name to appear on the Reddit leaderboard")
                    @NotNull String author,
                    @Description("Puzzle name. Can be shortened or abbreviated. E.g. `sus beha`, `OPAS`")
                    @Converter(ScPuzzleConverter.class) ScPuzzle puzzle,
                    @Description("Skips running SChem on the solutions if not empty. Admin-only")
                            String bypassValidation) {
            this.video = video;
            this.export = export;
            this.author = author;
            this.puzzle = puzzle;
            this.bypassValidation = bypassValidation;
        }
    }
}
