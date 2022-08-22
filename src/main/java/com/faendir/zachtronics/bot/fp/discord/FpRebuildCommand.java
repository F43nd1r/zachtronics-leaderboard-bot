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
import com.faendir.zachtronics.bot.discord.command.AbstractRebuildCommand;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.fp.FpQualifier;
import com.faendir.zachtronics.bot.fp.model.FpPuzzle;
import com.faendir.zachtronics.bot.fp.repository.FpSolutionRepository;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FpQualifier
public class FpRebuildCommand extends AbstractRebuildCommand<FpRebuildCommand.RebuildData, FpPuzzle> {
    @Delegate
    private final FpRebuildCommand_RebuildDataParser parser = FpRebuildCommand_RebuildDataParser.INSTANCE;
    @Getter
    private final Secured secured = FpSecured.ADMINS_ONLY;
    @Getter
    private final FpSolutionRepository repository;

    @NotNull
    @Override
    protected FpPuzzle findPuzzle(@NotNull RebuildData parameters) {
        return parameters.puzzle;
    }

    @ApplicationCommand(name = "rebuild", description = "Rebuilds wiki section", subCommand = true)
    @Value
    public static class RebuildData {
        @NotNull FpPuzzle puzzle;

        public RebuildData(@Description("Puzzle name. Can be shortened or abbreviated. E.g. `sus beha`, `OPAS`")
                           @AutoComplete(FpPuzzleAutoCompletionProvider.class)
                           @Converter(FpPuzzleConverter.class) @NonNull FpPuzzle puzzle) {
            this.puzzle = puzzle;
        }
    }
}
