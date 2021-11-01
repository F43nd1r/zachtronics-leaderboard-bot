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
import com.faendir.zachtronics.bot.archive.Archive;
import com.faendir.zachtronics.bot.discord.command.AbstractRetrieveCommand;
import com.faendir.zachtronics.bot.sz.SzQualifier;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzSolution;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@SzQualifier
public class SzRetrieveCommand extends AbstractRetrieveCommand<SzRetrieveCommand.RetrieveData, SzPuzzle, SzSolution> {
    @Delegate
    private final SzRetrieveCommand_RetrieveDataParser parser = SzRetrieveCommand_RetrieveDataParser.INSTANCE;
    @Getter
    private final Archive<SzPuzzle, SzSolution> archive;

    @NotNull
    @Override
    public SzPuzzle findPuzzle(@NotNull RetrieveData parameters) {
        return parameters.puzzle;
    }

    @ApplicationCommand(name = "retrieve", description = "Retrieve archived solutions", subCommand = true)
    @Value
    public static class RetrieveData {
        @NonNull SzPuzzle puzzle;

        public RetrieveData(@Description("Puzzle name. Can be shortened or abbreviated. E.g. `sus beha`, `OPAS`")
                        @Converter(SzPuzzleConverter.class) @NonNull SzPuzzle puzzle) {
            this.puzzle = puzzle;
        }
    }
}
