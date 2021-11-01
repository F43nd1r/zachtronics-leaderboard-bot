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
import com.faendir.zachtronics.bot.discord.LinkConverter;
import com.faendir.zachtronics.bot.discord.command.AbstractArchiveCommand;
import com.faendir.zachtronics.bot.sc.ScQualifier;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScSubmission;
import com.faendir.zachtronics.bot.sc.repository.ScSolutionRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@ScQualifier
public class ScArchiveCommand extends AbstractArchiveCommand<ScArchiveCommand.ArchiveData, ScCategory, ScSubmission> implements ScSecured {
    @Delegate
    private final ScArchiveCommand_ArchiveDataParser parser = ScArchiveCommand_ArchiveDataParser.INSTANCE;
    @Getter
    private final ScSolutionRepository repository;

    @NotNull
    @Override
    public List<ScSubmission> parseSubmissions(@NotNull ArchiveData parameters) {
        boolean bypassValidation = parameters.bypassValidation != null && parameters.bypassValidation;
        return ScSubmission.fromExportLink(parameters.export, parameters.puzzle, bypassValidation);
    }

    @ApplicationCommand(name = "archive", description = "Archive any number of solutions in an export file", subCommand = true)
    @Value
    @NonFinal
    public static class ArchiveData {
        @NotNull String export;
        ScPuzzle puzzle;
        Boolean bypassValidation;

        public ArchiveData(@NotNull
                           @Description("Link or `m1` to scrape it from your last message. " +
                                        "Start the solution name with `/B?P?` to set flags")
                           @Converter(LinkConverter.class) String export,
                           @Description("Puzzle name. Can be shortened or abbreviated. E.g. `sus beha`, `OPAS`")
                           @Converter(ScPuzzleConverter.class) ScPuzzle puzzle,
                           @Description("Skips running SChem on the solutions. Admin-only")
                           @Converter(value=ScAdminOnlyBooleanConverter.class, input=Boolean.class) Boolean bypassValidation) {
            this.export = export;
            this.puzzle = puzzle;
            this.bypassValidation = bypassValidation;
        }
    }
}
