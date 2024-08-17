/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.exa.discord;

import com.faendir.zachtronics.bot.discord.command.AbstractFrontierCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;
import com.faendir.zachtronics.bot.exa.ExaQualifier;
import com.faendir.zachtronics.bot.exa.model.ExaCategory;
import com.faendir.zachtronics.bot.exa.model.ExaPuzzle;
import com.faendir.zachtronics.bot.exa.model.ExaRecord;
import com.faendir.zachtronics.bot.exa.repository.ExaSolutionRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@ExaQualifier
public class ExaFrontierCommand extends AbstractFrontierCommand<ExaCategory, ExaPuzzle, ExaRecord> {
    @Getter
    private final CommandOption<String, ExaPuzzle> puzzleOption = OptionHelpersKt.enumOptionBuilder("puzzle", ExaPuzzle.class, ExaPuzzle::getDisplayName)
            .description("Puzzle name. Can be shortened or abbreviated. E.g. `fake surv`, `HD`")
            .required()
            .build();
    @Getter
    private final ExaSolutionRepository repository;
}
