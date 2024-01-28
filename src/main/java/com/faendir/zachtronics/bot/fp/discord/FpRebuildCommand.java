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

import com.faendir.zachtronics.bot.discord.command.AbstractRebuildCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.fp.FpQualifier;
import com.faendir.zachtronics.bot.fp.model.FpPuzzle;
import com.faendir.zachtronics.bot.fp.repository.FpSolutionRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FpQualifier
public class FpRebuildCommand extends AbstractRebuildCommand<FpPuzzle> {
    @Getter
    private final CommandOption<String, FpPuzzle> puzzleOption = OptionHelpersKt.enumOptionBuilder("puzzle", FpPuzzle.class, FpPuzzle::getDisplayName)
            .description("Puzzle name. Can be shortened or abbreviated. E.g. `1-1`, `add 2-3`")
            .build();
    @Getter
    private final Secured secured = FpSecured.ADMINS_ONLY;
    @Getter
    private final FpSolutionRepository repository;
}
