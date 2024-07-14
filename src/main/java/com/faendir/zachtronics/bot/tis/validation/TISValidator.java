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

package com.faendir.zachtronics.bot.tis.validation;

import com.faendir.zachtronics.bot.tis.model.TISPuzzle;
import com.faendir.zachtronics.bot.tis.model.TISScore;
import com.faendir.zachtronics.bot.tis.model.TISSubmission;
import com.faendir.zachtronics.bot.tis.model.TISType;
import com.faendir.zachtronics.bot.validation.ValidationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class TISValidator {
    private TISValidator() {}

    /**
     * @param score it will get patched to <tt>/a</tt> if the solution actually gets an achievement
     */
    @NotNull
    public static TISSubmission validate(@NotNull String data, @NotNull TISPuzzle puzzle, @NotNull TISScore score, @NotNull String author,
                                         String displayLink) {
        if (puzzle.getType() == TISType.SANDBOX)
            throw new ValidationException("Sandbox levels are not supported");

        TISSave save = TISSave.unmarshal(data);

        if (save.getNodes() != score.getNodes())
            throw new ValidationException("Solution has " + save.getNodes() + " nodes, score has " + score.getNodes());
        if (save.getInstructions() != score.getInstructions())
            throw new ValidationException("Solution has " + save.getInstructions() + " instructions, score has " + score.getInstructions());

        Boolean completesAchievement = completesAchievement(puzzle, save, score);
        if (completesAchievement != null) {
            if (score.isAchievement() && !completesAchievement)
                throw new ValidationException("Score declares to get an achievement, but the solution doesn't respect its constraint");
            if (!score.isAchievement() && completesAchievement)
                score = score.withAchievement(true);
        }

        return new TISSubmission(puzzle, score, author, displayLink, data);
    }

    /** @see <a href="https://steamcommunity.com/stats/370360/achievements">Steam Achievements page</a> */
    private static @Nullable Boolean completesAchievement(@NotNull TISPuzzle puzzle, @NotNull TISSave save, @NotNull TISScore score) {
        switch (puzzle) {
            case SELF_TEST_DIAGNOSTIC -> {
                return score.getCycles() > 100_000;
            }
            case SIGNAL_COMPARATOR -> {
                Pattern jxx = Pattern.compile(".*\\b(?:JGZ|JLZ|JEZ|JNZ)\\b.*", Pattern.CASE_INSENSITIVE);
                return save.codeAsStream().noneMatch(c -> jxx.matcher(c).matches());
            }
            case SEQUENCE_REVERSER -> {
                // one day...
                return null;
            }
            default -> {
                return false;
            }
        }
    }
}
