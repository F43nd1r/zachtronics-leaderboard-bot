/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.exa.discord;

import com.faendir.zachtronics.bot.discord.command.AbstractSubmitCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder;
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.exa.ExaQualifier;
import com.faendir.zachtronics.bot.exa.model.ExaCategory;
import com.faendir.zachtronics.bot.exa.model.ExaPuzzle;
import com.faendir.zachtronics.bot.exa.model.ExaRecord;
import com.faendir.zachtronics.bot.exa.model.ExaSubmission;
import com.faendir.zachtronics.bot.exa.repository.ExaSolutionRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@ExaQualifier
public class ExaSubmitCommand extends AbstractSubmitCommand<ExaCategory, ExaPuzzle, ExaSubmission, ExaRecord> {
    private final CommandOption<String, String> solutionOption = OptionHelpersKt.dataLinkOptionBuilder("solution")
            .description("Link to the solution file, can be `m1` to scrape it from your last message")
            .required()
            .build();
    private final CommandOption<Boolean, Boolean> cheesyOption = CommandOptionBuilder.bool("cheesy")
            .description("Does the solution use *cheesy* strategies? Be honest!")
            .required()
            .build();
    private final CommandOption<String, String> authorOption = CommandOptionBuilder.string("author")
            .description("Name to appear on the Reddit leaderboard")
            .required()
            .build();
    private final CommandOption<String, String> imageOption = OptionHelpersKt.displayLinkOptionBuilder("image")
            .description("Link to your image of the solution")
            .build();
    @Getter
    private final List<CommandOption<?, ?>> options = List.of(solutionOption, cheesyOption, authorOption, imageOption);
    @Getter
    private final Secured secured = ExaSecured.INSTANCE;
    @Getter
    private final ExaSolutionRepository repository;

    @NotNull
    @Override
    public ExaSubmission parseSubmission(@NotNull ChatInputInteractionEvent event) {
        return ExaSubmission.fromLink(solutionOption.get(event), cheesyOption.get(event), authorOption.get(event), imageOption.get(event));
    }
}
