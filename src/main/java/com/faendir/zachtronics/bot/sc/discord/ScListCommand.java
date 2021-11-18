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
import com.faendir.zachtronics.bot.discord.command.AbstractListCommand;
import com.faendir.zachtronics.bot.sc.ScQualifier;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.repository.ScSolutionRepository;
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
@ScQualifier
public class ScListCommand extends AbstractListCommand<ScListCommand.ListData, ScCategory, ScPuzzle, ScRecord> {
    @Delegate
    private final ScListCommand_ListDataParser parser = ScListCommand_ListDataParser.INSTANCE;
    @Getter
    private final ScSolutionRepository repository;

    @NotNull
    @Override
    public ScPuzzle findPuzzle(@NotNull ListData parameters) {
        return parameters.puzzle;
    }

    @ApplicationCommand(name = "list", description = "List records", subCommand = true)
    @Value
    public static class ListData {
        @NonNull ScPuzzle puzzle;

        public ListData(@Description("Puzzle name. Can be shortened or abbreviated. E.g. `sus beha`, `OPAS`")
                        @Converter(ScPuzzleConverter.class) @NonNull ScPuzzle puzzle) {
            this.puzzle = puzzle;
        }
    }
}
