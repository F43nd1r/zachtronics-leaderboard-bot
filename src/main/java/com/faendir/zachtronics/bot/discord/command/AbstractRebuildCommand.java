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

package com.faendir.zachtronics.bot.discord.command;

import com.faendir.zachtronics.bot.discord.Colors;
import com.faendir.zachtronics.bot.model.Puzzle;
import com.faendir.zachtronics.bot.repository.AbstractSolutionRepository;
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder;
import com.faendir.zachtronics.bot.utils.SafeMessageBuilder;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractRebuildCommand<P extends Puzzle<?>> extends Command.BasicLeaf {
    @Getter
    private final String name = "rebuild";
    @Getter
    private final String description = "Rebuilds wiki section";

    protected abstract AbstractSolutionRepository<?, P, ?, ?, ?, ?> getRepository();

    @NotNull
    @Override
    public SafeMessageBuilder handleEvent(@NotNull ChatInputInteractionEvent event) {
        P puzzle = findPuzzle(event);
        String updateMessage = "Rebuilt wiki section of " + puzzle.getDisplayName();
        getRepository().rebuildRedditLeaderboard(puzzle, updateMessage);
        return new SafeEmbedMessageBuilder()
                .title(updateMessage)
                .color(Colors.SUCCESS);
    }

    @NotNull
    protected abstract P findPuzzle(@NotNull ChatInputInteractionEvent event);
}
