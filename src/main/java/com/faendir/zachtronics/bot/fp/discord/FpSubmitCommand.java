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

package com.faendir.zachtronics.bot.fp.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.discord4j.command.annotation.Description;
import com.faendir.zachtronics.bot.discord.LinkConverter;
import com.faendir.zachtronics.bot.discord.command.AbstractMultiSubmitCommand;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.fp.FpQualifier;
import com.faendir.zachtronics.bot.fp.model.FpCategory;
import com.faendir.zachtronics.bot.fp.model.FpPuzzle;
import com.faendir.zachtronics.bot.fp.model.FpRecord;
import com.faendir.zachtronics.bot.fp.model.FpSubmission;
import com.faendir.zachtronics.bot.fp.repository.FpSolutionRepository;
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

@RequiredArgsConstructor
@Component
@FpQualifier
public class FpSubmitCommand extends AbstractMultiSubmitCommand<FpSubmitCommand.SubmitData, FpCategory, FpPuzzle, FpSubmission, FpRecord> {
    @Delegate
    private final FpSubmitCommand_SubmitDataParser parser = FpSubmitCommand_SubmitDataParser.INSTANCE;
    @Getter
    private final Secured secured = FpSecured.INSTANCE;
    @Getter
    private final FpSolutionRepository repository;

    @NotNull
    @Override
    public Collection<ValidationResult<FpSubmission>> parseSubmissions(@NotNull SubmitData parameters) {
        if (parameters.solution.equals(parameters.image))
            throw new IllegalArgumentException("Solution link and image link cannot be the same link");

        Collection<ValidationResult<FpSubmission>> results = FpSubmission.fromLink(parameters.solution, parameters.author);
        if (parameters.image != null) {
            if (results.size() != 1)
                throw new IllegalArgumentException("Only one solution can be paired with an image");

            ValidationResult<FpSubmission> result = results.iterator().next();
            if (result instanceof ValidationResult.Valid<FpSubmission>) {
                FpSubmission submission = result.getSubmission();
                FpSubmission imageSubmission = submission.withDisplayLink(parameters.image);
                return Collections.singleton(new ValidationResult.Valid<>(imageSubmission));
            }
            else {
                throw new IllegalArgumentException(result.getMessage());
            }
        }
        else
            return results;
    }

    @ApplicationCommand(name = "submit", subCommand = true)
    @Value
    public static class SubmitData {
        @NotNull String solution;
        @NotNull String author;
        @Nullable String image;

        public SubmitData(@Description("Link to the solution file, can be `m1` to scrape it from your last message")
                          @NotNull @Converter(LinkConverter.class) String solution,
                          @NotNull @Description("Name to appear on the Reddit leaderboard") String author,
                          @Description("Link to your image of the solution, can be `m1` to scrape it from your last message")
                          @Nullable @Converter(LinkConverter.class) String image) {
            this.solution = solution;
            this.author = author;
            this.image = image;
        }
    }
}
