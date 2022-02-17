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
import com.faendir.discord4j.command.annotation.AutoComplete;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.discord4j.command.annotation.Description;
import com.faendir.zachtronics.bot.discord.command.AbstractFrontierCommand;
import com.faendir.zachtronics.bot.inf.IfQualifier;
import com.faendir.zachtronics.bot.inf.model.IfCategory;
import com.faendir.zachtronics.bot.inf.model.IfPuzzle;
import com.faendir.zachtronics.bot.inf.model.IfRecord;
import com.faendir.zachtronics.bot.inf.repository.IfSolutionRepository;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@IfQualifier
public class IfFrontierCommand extends AbstractFrontierCommand<IfFrontierCommand.FrontierData, IfCategory, IfPuzzle, IfRecord> {
    @Delegate
    private final IfFrontierCommand_FrontierDataParser parser = IfFrontierCommand_FrontierDataParser.INSTANCE;
    @Getter
    private final IfSolutionRepository repository;

    @NotNull
    @Override
    public IfPuzzle findPuzzle(@NotNull FrontierData parameters) {
        return parameters.puzzle;
    }

    @ApplicationCommand(name = "frontier", description = "Displays the whole pareto frontier", subCommand = true)
    @Value
    public static class FrontierData {
        @NonNull IfPuzzle puzzle;

        public FrontierData(@Description("Puzzle name. Can be shortened or abbreviated. E.g. `Gne ch`, `TBB`")
                            @AutoComplete(IfPuzzleAutoCompletionProvider.class)
                            @Converter(IfPuzzleConverter.class) @NonNull IfPuzzle puzzle) {
            this.puzzle = puzzle;
        }
    }
}
