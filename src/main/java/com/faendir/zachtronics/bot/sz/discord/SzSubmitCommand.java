/*
 * Copyright (c) 2021
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

package com.faendir.zachtronics.bot.sz.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.discord4j.command.annotation.Description;
import com.faendir.zachtronics.bot.discord.LinkConverter;
import com.faendir.zachtronics.bot.discord.command.AbstractSubmitCommand;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.sz.SzQualifier;
import com.faendir.zachtronics.bot.sz.model.SzCategory;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzRecord;
import com.faendir.zachtronics.bot.sz.model.SzSubmission;
import com.faendir.zachtronics.bot.sz.repository.SzSolutionRepository;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@SzQualifier
public class SzSubmitCommand extends AbstractSubmitCommand<SzSubmitCommand.SubmitData, SzCategory, SzPuzzle, SzSubmission, SzRecord> {
    @Delegate
    private final SzSubmitCommand_SubmitDataParser parser = SzSubmitCommand_SubmitDataParser.INSTANCE;
    @Getter
    private final Secured secured = SzSecured.INSTANCE;
    @Getter
    private final SzSolutionRepository repository;

    @NotNull
    @Override
    public SzSubmission parseSubmission(@NotNull DeferrableInteractionEvent event, @NotNull SzSubmitCommand.SubmitData parameters) {
        return SzSubmission.fromLink(parameters.getSolution(), parameters.getAuthor());
    }

    @ApplicationCommand(name = "submit", subCommand = true)
    @Value
    public static class SubmitData {
        @NotNull String solution;
        @NotNull String author;

        public SubmitData(@Description("Link to the solution file, can be `m1` to scrape it from your last message")
                          @NotNull @Converter(LinkConverter.class) String solution,
                          @NotNull @Description("Name to appear on the Reddit leaderboard") String author) {
            this.solution = solution;
            this.author = author;
        }
    }
}
