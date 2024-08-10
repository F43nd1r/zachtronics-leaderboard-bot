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

package com.faendir.zachtronics.bot.tis.discord;

import com.faendir.zachtronics.bot.discord.command.AbstractSubmitCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder;
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.tis.TISQualifier;
import com.faendir.zachtronics.bot.tis.model.TISCategory;
import com.faendir.zachtronics.bot.tis.model.TISPuzzle;
import com.faendir.zachtronics.bot.tis.model.TISRecord;
import com.faendir.zachtronics.bot.tis.model.TISSubmission;
import com.faendir.zachtronics.bot.tis.repository.TISSolutionRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@TISQualifier
public class TISSubmitCommand extends AbstractSubmitCommand<TISCategory, TISPuzzle, TISSubmission, TISRecord> {
    private final CommandOption<String, String> solutionOption = OptionHelpersKt.dataLinkOptionBuilder("solution")
            .description("Link to the solution file, can be `m1` to scrape it from your last message")
            .required()
            .build();
    private final CommandOption<String, String> authorOption = CommandOptionBuilder.string("author")
            .description("Name to appear on the Reddit leaderboard")
            .required()
            .build();
    private final CommandOption<String, TISPuzzle> puzzleOption = OptionHelpersKt.enumOptionBuilder("puzzle", TISPuzzle.class, TISPuzzle::getDisplayName)
            .description("Puzzle name. Can be shortened or abbreviated. E.g. `SIGN AMPL`, `ITP1`")
            .build();
    private final CommandOption<String, String> imageOption = OptionHelpersKt.displayLinkOptionBuilder("image")
            .description("Link to your image of the solution")
            .build();
    @Getter
    private final List<CommandOption<?, ?>> options = List.of(solutionOption, authorOption, puzzleOption, imageOption);
    @Getter
    private final Secured secured = TISSecured.INSTANCE;
    @Getter
    private final TISSolutionRepository repository;

    @NotNull
    @Override
    public TISSubmission parseSubmission(@NotNull ChatInputInteractionEvent event) {
        return TISSubmission.fromLink(solutionOption.get(event), puzzleOption.get(event), authorOption.get(event), imageOption.get(event));
    }
}
