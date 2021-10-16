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
import com.faendir.zachtronics.bot.discord.LinkConverter;
import com.faendir.zachtronics.bot.discord.command.AbstractSubmitCommand;
import com.faendir.zachtronics.bot.leaderboards.Leaderboard;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import kotlin.Pair;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScSubmitCommand extends AbstractSubmitCommand<ScSubmitCommand.SubmitData, ScPuzzle, ScRecord> implements ScSecured {
    @Delegate
    private final ScSubmitCommand_SubmitDataParser parser = ScSubmitCommand_SubmitDataParser.INSTANCE;
    @Getter
    private final List<Leaderboard<?, ScPuzzle, ScRecord>> leaderboards;

    @NotNull
    @Override
    public Pair<ScPuzzle, ScRecord> parseSubmission(@NotNull SubmitData parameters) {
        ScRecord record = new ScRecord(parameters.score, parameters.author, parameters.video, "", false);
        return new Pair<>(parameters.puzzle, record);
    }

    @ApplicationCommand(subCommand = true)
    @Value
    public static class SubmitData {
        @NonNull ScPuzzle puzzle;
        @NotNull ScScore score;
        @NotNull String author;
        @NotNull String video;

        public SubmitData(@Converter(ScPuzzleConverter.class) @NonNull ScPuzzle puzzle,
                          @Converter(ScBPScoreConverter.class) @NonNull ScScore score,
                          @NotNull String author,
                          @NotNull @Converter(LinkConverter.class) String video) {
            this.puzzle = puzzle;
            this.score = score;
            this.video = video;
            this.author = author;
        }
    }
}
