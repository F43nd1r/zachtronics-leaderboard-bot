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

package com.faendir.zachtronics.bot.inf.discord;

import com.faendir.zachtronics.bot.discord.command.AbstractSubmitCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder;
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.inf.IfQualifier;
import com.faendir.zachtronics.bot.inf.model.*;
import com.faendir.zachtronics.bot.inf.repository.IfSolutionRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@IfQualifier
public class IfSubmitCommand extends AbstractSubmitCommand<IfCategory, IfPuzzle, IfSubmission, IfRecord> {
    private final CommandOption<String, String> solutionOption = OptionHelpersKt.linkOptionBuilder("solution")
            .description("Link or `m1` to scrape it from your last message.")
            .required()
            .build();
    private final CommandOption<String, IfScore> scoreOption = CommandOptionBuilder.string("score")
            .description("Score of the solution in CCC/FF/BB[/G] format")
            .required()
            .convert((event, score) -> IfScore.parseScore(score))
            .build();
    private final CommandOption<String, String> authorOption = CommandOptionBuilder.string("author")
            .description("Name to appear on the Reddit leaderboard")
            .required()
            .build();
    private static final String SEPARATOR = ",";
    private final CommandOption<String, List<String>> videosOption = OptionHelpersKt.linkOptionBuilder("videos")
            .description("Link(s) to the video(s) of the solution, accepts multiple separated by `,`")
            .convert((event, links) -> List.of(links.split(SEPARATOR)))
            .build();
    @Getter
    private final List<CommandOption<?, ?>> options = List.of(solutionOption, scoreOption, authorOption, videosOption);
    @Getter
    private final Secured secured = IfSecured.INSTANCE;
    @Getter
    private final IfSolutionRepository repository;

    @NotNull
    @Override
    public IfSubmission parseSubmission(@NotNull ChatInputInteractionEvent event) {
        return IfSubmission.fromLink(solutionOption.get(event), authorOption.get(event), scoreOption.get(event), videosOption.get(event));
    }
}
