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
import kotlin.Triple;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ScQualifier
public class ScSubmitArchiveCommand
        extends AbstractSubmitArchiveCommand<ScSubmitArchiveCommand.SubmitArchiveData, ScPuzzle, ScRecord, ScSolution>
        implements ScSecured {
    @Delegate
    private final ScSubmitArchiveCommand_SubmitArchiveDataParser parser = ScSubmitArchiveCommand_SubmitArchiveDataParser.INSTANCE;
    @Getter
    private final ScSubmitCommand submitCommand;
    @Getter
    private final ScArchiveCommand archiveCommand;

    @NotNull
    @Override
    public Triple<ScPuzzle, ScRecord, ScSolution> parseToPRS(@NotNull SubmitArchiveData parameters) {
        if (parameters.getExport().equals(parameters.video))
            throw new IllegalArgumentException("Export link and video link cannot be the same link");
        ScSolution solution = archiveCommand.parseSolutions(parameters).get(0);
        String archiveLink = archiveCommand.getArchive().makeArchiveLink(solution);
        ScRecord record = new ScRecord(solution.getScore(), parameters.author, parameters.video, archiveLink, false);
        return new Triple<>(solution.getPuzzle(), record, solution);
    }

    @ApplicationCommand(name = "submit-archive", description = "Submit and archive a solution", subCommand = true)
    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class SubmitArchiveData extends ScArchiveCommand.ArchiveData {
        @NotNull String video;
        @NotNull String author;

        public SubmitArchiveData(@Description("Link to your video of the solution, can be `m1` to scrape it from your last message")
                                 @NotNull @Converter(LinkConverter.class) String video,
                                 @Description("Link or `m1` to scrape it from your last message. " +
                                 "Start the solution name with `/B?P?` to set flags") @NotNull
                                 @Converter(LinkConverter.class) String export,
                                 @Description("Name to appear on the Reddit leaderboard")
                                 @NotNull String author,
                                 @Description("Puzzle name. Can be shortened or abbreviated. E.g. `sus beha`, `OPAS`")
                                 @Converter(ScPuzzleConverter.class) ScPuzzle puzzle,
                                 @Description("Skips running SChem on the solutions. Admin-only")
                                 @Converter(ScAdminOnlyBooleanConverter.class) Boolean bypassValidation) {
            super(export, puzzle, bypassValidation);
            this.video = video;
            this.author = author;
        }
    }
}
