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
import com.faendir.zachtronics.bot.discord.command.AbstractSubmitCommand;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.sc.ScQualifier;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScSubmission;
import com.faendir.zachtronics.bot.sc.repository.ScSolutionRepository;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
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
public class ScSubmitCommand extends AbstractSubmitCommand<ScSubmitCommand.SubmitData, ScCategory, ScPuzzle, ScSubmission, ScRecord> {
    @Delegate
    private final ScSubmitCommand_SubmitDataParser parser = ScSubmitCommand_SubmitDataParser.INSTANCE;
    @Getter
    private final Secured secured = ScSecured.INSTANCE;
    @Getter
    private final ScSolutionRepository repository;
    @Getter
    private final ScArchiveCommand archiveCommand;

    @NotNull
    @Override
    public ScSubmission parseSubmission(@NotNull DeferrableInteractionEvent event, @NotNull SubmitData parameters) {
        if (parameters.getExport().equals(parameters.video))
            throw new IllegalArgumentException("Export link and video link cannot be the same link");
        ValidationResult<ScSubmission> result = archiveCommand.parseSubmissions(parameters).iterator().next();
        if (result instanceof ValidationResult.Valid<ScSubmission>) {
            ScSubmission submission = result.getSubmission();
            return new ScSubmission(submission.getPuzzle(), submission.getScore(), parameters.author, parameters.video,
                                    submission.getData());
        }
        else {
            throw new IllegalArgumentException(result.getMessage());
        }
    }

    @ApplicationCommand(name = "submit", description = "Submit and archive a solution", subCommand = true)
    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class SubmitData extends ScArchiveCommand.ArchiveData {
        @NotNull String video;
        @NotNull String author;

        public SubmitData(@Description("Link to your video of the solution, can be `m1` to scrape it from your last message")
                          @NotNull @Converter(LinkConverter.class) String video,
                          @Description("Link or `m1` to scrape it from your last message. " +
                                       "Start the solution name with `/B?P?` to set flags")
                          @NotNull @Converter(LinkConverter.class) String export,
                          @Description("Name to appear on the Reddit leaderboard")
                          @NotNull String author,
                          @Description("Skips running SChem on the solutions. Admin-only")
                          @Converter(value = ScAdminOnlyBooleanConverter.class, input = Boolean.class)
                                  Boolean bypassValidation) {
            super(export, bypassValidation);
            this.video = video;
            this.author = author;
        }
    }
}
