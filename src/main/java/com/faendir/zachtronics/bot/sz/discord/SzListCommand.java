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
import com.faendir.zachtronics.bot.discord.command.AbstractListCommand;
import com.faendir.zachtronics.bot.sz.SzQualifier;
import com.faendir.zachtronics.bot.sz.model.SzCategory;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzRecord;
import com.faendir.zachtronics.bot.sz.repository.SzSolutionRepository;
import kotlin.Pair;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Component
@SzQualifier
public class SzListCommand extends AbstractListCommand<SzListCommand.ListData, SzCategory, SzPuzzle, SzRecord> {
    @Delegate
    private final SzListCommand_ListDataParser parser = SzListCommand_ListDataParser.INSTANCE;
    @Getter
    private final SzSolutionRepository repository;

    @NotNull
    @Override
    public Pair<SzPuzzle, List<SzCategory>> findPuzzleAndCategories(@NotNull ListData parameters) {
        return new Pair<>(parameters.puzzle,
                          Arrays.stream(SzCategory.values())
                                .filter(c -> c.supportsPuzzle(parameters.puzzle))
                                .toList());
    }

    @ApplicationCommand(name = "list", subCommand = true)
    @Value
    public static class ListData {
        @NonNull SzPuzzle puzzle;

        public ListData(@Converter(SzPuzzleConverter.class) @NonNull SzPuzzle puzzle) {
            this.puzzle = puzzle;
        }
    }
}
