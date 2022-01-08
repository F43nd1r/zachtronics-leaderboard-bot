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

package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.discord4j.command.annotation.Description;
import com.faendir.zachtronics.bot.discord.Colors;
import com.faendir.zachtronics.bot.discord.command.AbstractSubCommand;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.sc.ScQualifier;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.repository.ScSolutionRepository;
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@ScQualifier
public class ScRebuildCommand extends AbstractSubCommand<ScRebuildCommand.RebuildData> {
    @Delegate
    private final ScRebuildCommand_RebuildDataParser parser = ScRebuildCommand_RebuildDataParser.INSTANCE;
    @Getter
    private final Secured secured = ScSecured.WIKI_ADMINS_ONLY;
    @Getter
    private final ScSolutionRepository repository;

    @NotNull
    @Override
    public Mono<Void> handle(@NotNull DeferrableInteractionEvent event, @NotNull RebuildData parameters) {
        String updateMessage = "Rebuilt wiki section of " + parameters.puzzle.getDisplayName();
        repository.rebuildRedditLeaderboard(parameters.puzzle, updateMessage);
        return new SafeEmbedMessageBuilder()
                .title(updateMessage)
                .color(Colors.SUCCESS)
                .send(event);
    }

    @ApplicationCommand(name = "rebuild", description = "Rebuilds wiki section", subCommand = true)
    @Value
    public static class RebuildData {
        @NotNull ScPuzzle puzzle;

        public RebuildData(@Description("Puzzle name. Can be shortened or abbreviated. E.g. `sus beha`, `OPAS`")
                           @Converter(ScPuzzleConverter.class) @NonNull ScPuzzle puzzle) {
            this.puzzle = puzzle;
        }
    }
}
