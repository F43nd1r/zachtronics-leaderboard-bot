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

package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.discord.command.AbstractMultiSubmitCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder;
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;
import com.faendir.zachtronics.bot.discord.command.security.NotSecured;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.sc.ScQualifier;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScSubmission;
import com.faendir.zachtronics.bot.sc.repository.ScSolutionRepository;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@ScQualifier
public class ScSubmitCommand extends AbstractMultiSubmitCommand<ScCategory, ScPuzzle, ScSubmission, ScRecord> {
    private final CommandOption<String, String> exportOption = OptionHelpersKt.dataLinkOptionBuilder("export")
            .description("Link or `m1` to scrape it from your last message. Start the solution name with `/B?P?` to set flags")
            .required()
            .build();
    private final CommandOption<String, String> authorOption = CommandOptionBuilder.string("author")
            .description("Name to appear on the Reddit leaderboard")
            .build();
    private final CommandOption<String, String> videoOption = OptionHelpersKt.displayLinkOptionBuilder("video")
            .description("Link to your video of the solution")
            .build();
    private final CommandOption<Boolean, Boolean> bypassValidationOption = CommandOptionBuilder.bool("bypass-validation")
            .description("Skips running SChem on the solutions. Admin-only")
            .convert((event, bypass) -> {
                if (bypass && !ScSecured.WIKI_ADMINS_ONLY.hasExecutionPermission(event)) {
                    throw new IllegalArgumentException("Only a wiki admin can use this parameter");
                }
                return bypass;
            })
            .build();
    @Getter
    private final List<CommandOption<?, ?>> options = List.of(exportOption, authorOption, videoOption, bypassValidationOption);
    @Getter
    private final Secured secured = NotSecured.INSTANCE;
    @Getter
    private final ScSolutionRepository repository;

    @NotNull
    @Override
    public Collection<ValidationResult<ScSubmission>> parseSubmissions(@NotNull ChatInputInteractionEvent event) {
        String export = exportOption.get(event);
        String video = videoOption.get(event);
        String author = authorOption.get(event);
        Boolean bypassValidationIn = bypassValidationOption.get(event);
        if (export.equals(video))
            throw new IllegalArgumentException("Export link and video link cannot be the same link");

        boolean bypassValidation = bypassValidationIn != null && bypassValidationIn;
        Collection<ValidationResult<ScSubmission>> results = ScSubmission.fromExportLink(export, bypassValidation, author);
        if (video != null) {
            if (results.size() != 1)
                throw new IllegalArgumentException("Only one solution can be paired with a video");

            ValidationResult<ScSubmission> result = results.iterator().next();
            if (result instanceof ValidationResult.Valid<ScSubmission>) {
                ScSubmission submission = result.getSubmission();
                ScSubmission videoSubmission = submission.withDisplayLink(video);
                return Collections.singleton(new ValidationResult.Valid<>(videoSubmission));
            } else {
                throw new IllegalArgumentException(result.getMessage());
            }
        } else
            return results;
    }
}
