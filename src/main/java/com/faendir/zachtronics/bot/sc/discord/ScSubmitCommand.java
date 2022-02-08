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

package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.discord4j.command.annotation.Description;
import com.faendir.zachtronics.bot.discord.LinkConverter;
import com.faendir.zachtronics.bot.discord.command.AbstractMultiSubmitCommand;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.sc.ScQualifier;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScSubmission;
import com.faendir.zachtronics.bot.sc.repository.ScSolutionRepository;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@ScQualifier
public class ScSubmitCommand extends AbstractMultiSubmitCommand<ScSubmitCommand.SubmitData, ScCategory, ScPuzzle, ScSubmission, ScRecord> {
    @Delegate
    private final ScSubmitCommand_SubmitDataParser parser = ScSubmitCommand_SubmitDataParser.INSTANCE;
    @Getter
    private final Secured secured = ScSecured.INSTANCE;
    @Getter
    private final ScSolutionRepository repository;

    @NotNull
    @Override
    public Collection<ValidationResult<ScSubmission>> parseSubmissions(@NotNull ScSubmitCommand.SubmitData parameters) {
        if (parameters.getExport().equals(parameters.video))
            throw new IllegalArgumentException("Export link and video link cannot be the same link");

        boolean bypassValidation = parameters.bypassValidation != null && parameters.bypassValidation;
        Collection<ValidationResult<ScSubmission>> results = ScSubmission.fromExportLink(parameters.export, bypassValidation,
                                                                                         parameters.author);
        if (parameters.video != null) {
            if (results.size() != 1)
                throw new IllegalArgumentException("Only one solution can be paired with a video");

            ValidationResult<ScSubmission> result = results.iterator().next();
            if (result instanceof ValidationResult.Valid<ScSubmission>) {
                ScSubmission submission = result.getSubmission();
                ScSubmission videoSubmission = submission.withDisplayLink(parameters.video);
                return Collections.singleton(new ValidationResult.Valid<>(videoSubmission));
            }
            else {
                throw new IllegalArgumentException(result.getMessage());
            }
        }
        else
            return results;
    }

    @ApplicationCommand(name = "submit", description = "Submit and archive any number of solutions", subCommand = true)
    @Value
    public static class SubmitData {
        @NotNull String export;
        @Nullable String author;
        @Nullable String video;
        @Nullable Boolean bypassValidation;

        public SubmitData(@Description("Link or `m1` to scrape it from your last message. " +
                                       "Start the solution name with `/B?P?` to set flags")
                          @NotNull @Converter(LinkConverter.class) String export,
                          @Description("Name to appear on the Reddit leaderboard")
                          @Nullable String author,
                          @Description("Link to your video of the solution, can be `m1` to scrape it from your last message")
                          @Nullable @Converter(LinkConverter.class) String video,
                          @Description("Skips running SChem on the solutions. Admin-only")
                          @Nullable @Converter(value = ScAdminOnlyBooleanConverter.class, input = Boolean.class)
                                  Boolean bypassValidation) {
            this.export = export;
            this.bypassValidation = bypassValidation;
            this.author = author;
            this.video = video;
        }
    }
}
