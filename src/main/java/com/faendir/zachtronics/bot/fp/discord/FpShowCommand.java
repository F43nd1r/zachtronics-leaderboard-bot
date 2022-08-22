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

package com.faendir.zachtronics.bot.fp.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.AutoComplete;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.discord4j.command.annotation.Description;
import com.faendir.zachtronics.bot.discord.command.AbstractShowCommand;
import com.faendir.zachtronics.bot.fp.FpQualifier;
import com.faendir.zachtronics.bot.fp.model.FpCategory;
import com.faendir.zachtronics.bot.fp.model.FpPuzzle;
import com.faendir.zachtronics.bot.fp.model.FpRecord;
import com.faendir.zachtronics.bot.fp.repository.FpSolutionRepository;
import kotlin.Pair;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@FpQualifier
public class FpShowCommand extends AbstractShowCommand<FpShowCommand.ShowData, FpCategory, FpPuzzle, FpRecord> {
    @Delegate
    private final FpShowCommand_ShowDataParser parser = FpShowCommand_ShowDataParser.INSTANCE;
    @Getter
    private final FpSolutionRepository repository;

    @NotNull
    @Override
    public Pair<FpPuzzle, FpCategory> findPuzzleAndCategory(@NotNull ShowData parameters) {
        FpPuzzle puzzle = parameters.puzzle;
        FpCategory category = parameters.category;
        if (!parameters.puzzle.getSupportedCategories().contains(category))
            throw new IllegalArgumentException(
                    "Category " + category.getDisplayName() + " does not support " + puzzle.getDisplayName());
        return new Pair<>(puzzle, category);
    }

    @ApplicationCommand(name = "show", subCommand = true)
    @Value
    public static class ShowData {
        @NonNull FpPuzzle puzzle;
        @NonNull FpCategory category;

        public ShowData(@Description("Puzzle name. Can be shortened or abbreviated. E.g. `fake surv`, `HD`")
                        @AutoComplete(FpPuzzleAutoCompletionProvider.class)
                        @Converter(FpPuzzleConverter.class) @NonNull FpPuzzle puzzle,
                        @Description("Category. E.g. `CP`, `LC`")
                        @NonNull FpCategory category) {
            this.puzzle = puzzle;
            this.category = category;
        }
    }
}
