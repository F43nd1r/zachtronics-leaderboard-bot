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

package com.faendir.zachtronics.bot.fp.discord;

import com.faendir.zachtronics.bot.discord.command.AbstractListCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.fp.FpQualifier;
import com.faendir.zachtronics.bot.fp.model.FpCategory;
import com.faendir.zachtronics.bot.fp.model.FpPuzzle;
import com.faendir.zachtronics.bot.fp.model.FpRecord;
import com.faendir.zachtronics.bot.fp.repository.FpSolutionRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@FpQualifier
public class FpListCommand extends AbstractListCommand<FpCategory, FpPuzzle, FpRecord> {
    @Getter
    private final CommandOption<String, FpPuzzle> puzzleOption = FpOptionBuilders.puzzleOptionBuilder().required().build();
    @Getter
    private final FpSolutionRepository repository;
}
