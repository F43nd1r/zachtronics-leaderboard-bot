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

package com.faendir.zachtronics.bot.kz.discord;

import com.faendir.zachtronics.bot.discord.command.AbstractSubmitCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.kz.KzQualifier;
import com.faendir.zachtronics.bot.kz.model.KzCategory;
import com.faendir.zachtronics.bot.kz.model.KzPuzzle;
import com.faendir.zachtronics.bot.kz.model.KzRecord;
import com.faendir.zachtronics.bot.kz.model.KzSubmission;
import com.faendir.zachtronics.bot.kz.repository.KzSolutionRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt.*;

@RequiredArgsConstructor
@Component
@KzQualifier
public class KzSubmitCommand extends AbstractSubmitCommand<KzCategory, KzPuzzle, KzSubmission, KzRecord> {
    private final CommandOption<String, String> solutionOption = solutionOptionBuilder().required().build();
    private final CommandOption<String, String> authorOption = authorOptionBuilder().required().build();
    private final CommandOption<String, String> imageOption = imageOptionBuilder().build();
    @Getter
    private final List<CommandOption<?, ?>> options = List.of(solutionOption, authorOption, imageOption);
    @Getter
    private final Secured secured = KzSecured.SUBMIT;
    @Getter
    private final KzSolutionRepository repository;

    @NotNull
    @Override
    public KzSubmission parseSubmission(@NotNull ChatInputInteractionEvent event) {
        return KzSubmission.fromLink(solutionOption.get(event), authorOption.get(event), imageOption.get(event));
    }
}
