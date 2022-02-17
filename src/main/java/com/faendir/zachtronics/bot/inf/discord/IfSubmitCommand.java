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

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.discord4j.command.annotation.Description;
import com.faendir.zachtronics.bot.discord.LinkConverter;
import com.faendir.zachtronics.bot.discord.ListConverter;
import com.faendir.zachtronics.bot.discord.command.AbstractSubmitCommand;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.inf.IfQualifier;
import com.faendir.zachtronics.bot.inf.model.*;
import com.faendir.zachtronics.bot.inf.repository.IfSolutionRepository;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@IfQualifier
public class IfSubmitCommand extends AbstractSubmitCommand<IfSubmitCommand.SubmitData, IfCategory, IfPuzzle, IfSubmission, IfRecord> {
    @Delegate
    private final IfSubmitCommand_SubmitDataParser parser = IfSubmitCommand_SubmitDataParser.INSTANCE;
    @Getter
    private final Secured secured = IfSecured.INSTANCE;
    @Getter
    private final IfSolutionRepository repository;

    @NotNull
    @Override
    public IfSubmission parseSubmission(@NotNull DeferrableInteractionEvent event, @NotNull IfSubmitCommand.SubmitData parameters) {
        return IfSubmission.fromLink(parameters.getSolution(), parameters.getAuthor(), parameters.getScore(), parameters.getVideos());
    }

    @ApplicationCommand(name = "submit", description = "Submit a solution", subCommand = true)
    @Value
    public static class SubmitData {
        @NotNull String solution;
        @NotNull IfScore score;
        @NotNull String author;
        @NotNull List<String> videos;

        public SubmitData(@Description("Link or `m1` to scrape it from your last message.")
                          @NotNull @Converter(LinkConverter.class) String solution,
                          @Description("Score of the solution in CCC/FF/BB[/G] format")
                          @NotNull @Converter(IfScoreConverter.class) IfScore score,
                          @Description("Name to appear on the Reddit leaderboard")
                          @NotNull String author,
                          @Description("Link(s) to the video(s) of the solution, accepts multiple separated by `,`")
                          @NotNull @Converter(ListConverter.class) List<String> videos) {
            this.solution = solution;
            this.score = score;
            this.author = author;
            this.videos = videos;
        }
    }
}
