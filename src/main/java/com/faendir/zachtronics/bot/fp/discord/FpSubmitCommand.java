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

import com.faendir.zachtronics.bot.discord.command.AbstractMultiSubmitCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder;
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.fp.FpQualifier;
import com.faendir.zachtronics.bot.fp.model.FpCategory;
import com.faendir.zachtronics.bot.fp.model.FpPuzzle;
import com.faendir.zachtronics.bot.fp.model.FpRecord;
import com.faendir.zachtronics.bot.fp.model.FpSubmission;
import com.faendir.zachtronics.bot.fp.repository.FpSolutionRepository;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
@FpQualifier
public class FpSubmitCommand extends AbstractMultiSubmitCommand<FpCategory, FpPuzzle, FpSubmission, FpRecord> {
    private final CommandOption<String, String> solutionOption = OptionHelpersKt.linkOptionBuilder("solution")
            .description("Link to the solution file, can be `m1` to scrape it from your last message")
            .required()
            .build();
    private final CommandOption<String, String> authorOption = CommandOptionBuilder.string("author")
            .description("Name to appear on the Reddit leaderboard")
            .required()
            .build();
    private final CommandOption<String, String> imageOption = OptionHelpersKt.linkOptionBuilder("image")
            .description("Link to your image of the solution, can be `m1` to scrape it from your last message")
            .build();
    @Getter
    private final List<CommandOption<?, ?>> options = List.of(solutionOption, authorOption, imageOption);
    @Getter
    private final Secured secured = FpSecured.INSTANCE;
    @Getter
    private final FpSolutionRepository repository;

    @NotNull
    @Override
    public Collection<ValidationResult<FpSubmission>> parseSubmissions(@NotNull ChatInputInteractionEvent event) {
        String solution = solutionOption.get(event);
        String author = authorOption.get(event);
        String image = imageOption.get(event);
        if (solution.equals(image))
            throw new IllegalArgumentException("Solution link and image link cannot be the same link");

        Collection<ValidationResult<FpSubmission>> results = FpSubmission.fromLink(solution, author);
        if (image != null) {
            if (results.size() != 1)
                throw new IllegalArgumentException("Only one solution can be paired with an image");

            ValidationResult<FpSubmission> result = results.iterator().next();
            if (result instanceof ValidationResult.Valid<FpSubmission>) {
                FpSubmission submission = result.getSubmission();
                FpSubmission imageSubmission = submission.withDisplayLink(image);
                return Collections.singleton(new ValidationResult.Valid<>(imageSubmission));
            } else {
                throw new IllegalArgumentException(result.getMessage());
            }
        } else
            return results;
    }
}
