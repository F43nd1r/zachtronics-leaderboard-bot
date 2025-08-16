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

package com.faendir.zachtronics.bot.exa.discord;

import com.faendir.zachtronics.bot.discord.command.AbstractShowCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.exa.ExaQualifier;
import com.faendir.zachtronics.bot.exa.model.ExaCategory;
import com.faendir.zachtronics.bot.exa.model.ExaPuzzle;
import com.faendir.zachtronics.bot.exa.model.ExaRecord;
import com.faendir.zachtronics.bot.exa.repository.ExaSolutionRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import kotlin.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@ExaQualifier
public class ExaShowCommand extends AbstractShowCommand<ExaCategory, ExaPuzzle, ExaRecord> {
    private final CommandOption<String, ExaPuzzle> puzzleOption = ExaOptionBuilders.puzzleOptionBuilder().required().build();
    private final CommandOption<String, ExaCategory> categoryOption = ExaOptionBuilders.categoryOptionBuilder().required().build();
    @Getter
    private final List<CommandOption<?, ?>> options = List.of(puzzleOption, categoryOption);
    @Getter
    private final ExaSolutionRepository repository;

    @NotNull
    @Override
    public Pair<ExaPuzzle, ExaCategory> findPuzzleAndCategory(@NotNull ChatInputInteractionEvent event) {
        ExaPuzzle puzzle = puzzleOption.get(event);
        ExaCategory category = categoryOption.get(event);
        if (!puzzle.getSupportedCategories().contains(category))
            throw new IllegalArgumentException(
                "Category " + category.getDisplayName() + " does not support " + puzzle.getDisplayName());
        return new Pair<>(puzzle, category);
    }
}
