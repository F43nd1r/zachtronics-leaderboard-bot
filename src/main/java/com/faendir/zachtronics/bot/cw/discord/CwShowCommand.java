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

package com.faendir.zachtronics.bot.cw.discord;

import com.faendir.zachtronics.bot.cw.CwQualifier;
import com.faendir.zachtronics.bot.cw.model.CwCategory;
import com.faendir.zachtronics.bot.cw.model.CwPuzzle;
import com.faendir.zachtronics.bot.cw.model.CwRecord;
import com.faendir.zachtronics.bot.cw.repository.CwSolutionRepository;
import com.faendir.zachtronics.bot.discord.command.AbstractShowCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import kotlin.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@CwQualifier
public class CwShowCommand extends AbstractShowCommand<CwCategory, CwPuzzle, CwRecord> {
    private final CommandOption<String, CwPuzzle> puzzleOption = CwOptionBuilders.PUZZLE_BUILDER.required().build();
    private final CommandOption<String, CwCategory> categoryOption = CwOptionBuilders.CATEGORY_BUILDER.required().build();
    @Getter
    private final List<CommandOption<?, ?>> options = List.of(puzzleOption, categoryOption);
    @Getter
    private final CwSolutionRepository repository;

    @NotNull
    @Override
    public Pair<CwPuzzle, CwCategory> findPuzzleAndCategory(@NotNull ChatInputInteractionEvent event) {
        CwPuzzle puzzle = puzzleOption.get(event);
        CwCategory category = categoryOption.get(event);
        if (!puzzle.getSupportedCategories().contains(category))
            throw new IllegalArgumentException(
                "Category " + category.getDisplayName() + " does not support " + puzzle.getDisplayName());
        return new Pair<>(puzzle, category);
    }
}
