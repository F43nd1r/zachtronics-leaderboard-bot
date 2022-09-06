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

import com.faendir.zachtronics.bot.discord.command.AbstractRebuildCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.inf.IfQualifier;
import com.faendir.zachtronics.bot.inf.model.IfPuzzle;
import com.faendir.zachtronics.bot.inf.repository.IfSolutionRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@IfQualifier
public class IfRebuildCommand extends AbstractRebuildCommand<IfPuzzle> {
    private final CommandOption<String, IfPuzzle> puzzleOption = OptionHelpersKt.enumOptionBuilder("puzzle", IfPuzzle.class, IfPuzzle::getDisplayName)
            .description("Puzzle name. Can be shortened or abbreviated. E.g. `Gne ch`, `TBB`")
            .required()
            .build();
    @Getter
    private final List<CommandOption<?, ?>> options = List.of(puzzleOption);
    @Getter
    private final Secured secured = IfSecured.ADMINS_ONLY;
    @Getter
    private final IfSolutionRepository repository;

    @NotNull
    @Override
    protected IfPuzzle findPuzzle(@NotNull ChatInputInteractionEvent event) {
        return puzzleOption.get(event);
    }
}
