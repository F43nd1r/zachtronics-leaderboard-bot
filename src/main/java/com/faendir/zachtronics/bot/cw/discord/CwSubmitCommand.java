/*
 * Copyright (c) 2025
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

package com.faendir.zachtronics.bot.cw.discord;

import com.faendir.zachtronics.bot.cw.CwQualifier;
import com.faendir.zachtronics.bot.cw.model.CwCategory;
import com.faendir.zachtronics.bot.cw.model.CwPuzzle;
import com.faendir.zachtronics.bot.cw.model.CwRecord;
import com.faendir.zachtronics.bot.cw.model.CwSubmission;
import com.faendir.zachtronics.bot.cw.repository.CwSolutionRepository;
import com.faendir.zachtronics.bot.discord.command.AbstractMultiSubmitCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder;
import com.faendir.zachtronics.bot.discord.command.security.NotSecured;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt.*;

@RequiredArgsConstructor
@Component
@CwQualifier
public class CwSubmitCommand extends AbstractMultiSubmitCommand<CwCategory, CwPuzzle, CwSubmission, CwRecord> {
    private static final String SOLUTION_PREFIX = "Volgograd.Solution.";

    private final CommandOption<String, String> solutionOption = CommandOptionBuilder.string("solution")
            .description("Link to the solution file, can be `m1` to scrape it from your last message or single solution text")
            .required()
            .convert((event, link) -> link.startsWith(SOLUTION_PREFIX) ? link : resolveLink(event, link, true))
            .build();
    private final CommandOption<String, String> authorOption = authorOptionBuilder().required().build();
    private final CommandOption<String, String> imageOption = imageOptionBuilder().build();
    @Getter
    private final List<CommandOption<?, ?>> options = List.of(solutionOption, authorOption, imageOption);
    @Getter
    private final Secured secured = NotSecured.INSTANCE;
    @Getter
    private final CwSolutionRepository repository;

    @NotNull
    @Override
    public Collection<ValidationResult<CwSubmission>> parseSubmissions(@NotNull ChatInputInteractionEvent event) {
        String solution = solutionOption.get(event);
        String author = authorOption.get(event);
        String image = imageOption.get(event);
        if (solution.equals(image))
            throw new IllegalArgumentException("Solution link and image link cannot be the same link");

        Collection<ValidationResult<CwSubmission>> results = solution.startsWith(SOLUTION_PREFIX) ?
                                                             CwSubmission.fromData(solution, author) :
                                                             CwSubmission.fromLink(solution, author);
        if (image != null) {
            if (results.size() != 1)
                throw new IllegalArgumentException("Only one solution can be paired with an image");

            ValidationResult<CwSubmission> result = results.iterator().next();
            if (result instanceof ValidationResult.Valid<CwSubmission>) {
                CwSubmission submission = result.getSubmission();
                CwSubmission imageSubmission = submission.withDisplayLink(image);
                return Collections.singleton(new ValidationResult.Valid<>(imageSubmission));
            } else {
                throw new IllegalArgumentException(result.getMessage());
            }
        } else
            return results;
    }
}
