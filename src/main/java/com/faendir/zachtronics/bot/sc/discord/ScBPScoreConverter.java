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

import com.faendir.discord4j.command.annotation.OptionConverter;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

public class ScBPScoreConverter implements OptionConverter<ScScore> {
    @NotNull
    @Override
    public ScScore fromString(@NotNull SlashCommandEvent context, @NotNull String s) {
        return makeScore(s);
    }

    @NotNull
    private static ScScore makeScore(String rawScore) {
        ScScore score = ScScore.parseBPScore(rawScore);
        if (score == null)
            throw new IllegalArgumentException();
        return score;
    }
}
