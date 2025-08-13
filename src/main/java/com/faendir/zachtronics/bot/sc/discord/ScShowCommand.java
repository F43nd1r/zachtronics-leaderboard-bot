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

import com.faendir.zachtronics.bot.discord.command.AbstractShowCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.sc.ScQualifier;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.repository.ScSolutionRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import kotlin.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@ScQualifier
public class ScShowCommand extends AbstractShowCommand<ScCategory, ScPuzzle, ScRecord> {
    private final CommandOption<String, ScPuzzle> puzzleOption = ScOptionBuilders.PUZZLE_BUILDER.required().build();
    private final CommandOption<String, ScCategory> categoryOption = ScOptionBuilders.CATEGORY_BUILDER.required().build();
    @Getter
    private final List<CommandOption<?, ?>> options = List.of(puzzleOption, categoryOption);
    @Getter
    private final ScSolutionRepository repository;

    @NotNull
    @Override
    public Pair<ScPuzzle, ScCategory> findPuzzleAndCategory(@NotNull ChatInputInteractionEvent event) {
        ScPuzzle puzzle = puzzleOption.get(event);
        ScCategory category = categoryOption.get(event);
        if (!puzzle.getSupportedCategories().contains(category))
            throw new IllegalArgumentException(
                "Category " + category.getDisplayName() + " does not support " + puzzle.getDisplayName());
        return new Pair<>(puzzle, category);
    }
}
