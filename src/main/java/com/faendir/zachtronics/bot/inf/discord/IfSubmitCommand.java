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

package com.faendir.zachtronics.bot.inf.discord;

import com.faendir.zachtronics.bot.discord.command.AbstractMultiSubmitCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.inf.IfQualifier;
import com.faendir.zachtronics.bot.inf.model.*;
import com.faendir.zachtronics.bot.inf.repository.IfSolutionRepository;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt.*;

@RequiredArgsConstructor
@Component
@IfQualifier
public class IfSubmitCommand extends AbstractMultiSubmitCommand<IfCategory, IfPuzzle, IfSubmission, IfRecord> {
    private final CommandOption<String, String> solutionOption = solutionOptionBuilder().required().build();
    private final CommandOption<String, String> authorOption = authorOptionBuilder().required().build();
    private final CommandOption<String, IfScore> scoreOption = CommandOptionBuilder.string("score")
            .description("Score of the solution in ccc/fff/bbb[/OGF] format")
            .convert((event, score) -> {
                IfScore ifScore = IfScore.parseScore(score);
                if (ifScore != null)
                    return ifScore;
                throw new IllegalArgumentException("Invalid score: " + score);
            })
            .build();
    private final CommandOption<String, List<String>> videosOption = CommandOptionBuilder.string("videos")
            .description("Link(s) to the video(s) of the solution, accepts multiple separated by `,`")
            .convert((event, links) -> Pattern.compile(",").splitAsStream(links)
                                              .map(l -> resolveLink(event, l, false))
                                              .toList())
            .build();
    @Getter
    private final List<CommandOption<?, ?>> options = List.of(solutionOption, authorOption, scoreOption, videosOption);
    @Getter
    private final Secured secured = IfSecured.SUBMIT;
    @Getter
    private final IfSolutionRepository repository;

    @NotNull
    @Override
    public Collection<ValidationResult<IfSubmission>> parseSubmissions(@NotNull ChatInputInteractionEvent event) {
        IfScore score = scoreOption.get(event);
        List<String> videos = videosOption.get(event);
        boolean isAdmin = IfSecured.WIKI_ADMINS_ONLY.hasExecutionPermission(event);

        return IfSubmission.fromLink(solutionOption.get(event), authorOption.get(event), score, videos, isAdmin);
    }
}
