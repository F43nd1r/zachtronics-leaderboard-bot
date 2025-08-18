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

package com.faendir.zachtronics.bot.sz.validation;

import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzScore;
import com.faendir.zachtronics.bot.sz.model.SzSubmission;
import com.faendir.zachtronics.bot.validation.ValidationException;
import org.jetbrains.annotations.NotNull;

public class SzValidator {
    private SzValidator() {}

    public static @NotNull SzSubmission validate(String data, String author, String displayLink) {
        SzSave save = SzSave.unmarshal(data);

        SzPuzzle puzzle;
        try {
            puzzle = SzPuzzle.valueOf(save.getPuzzle());
        }
        catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown puzzle: " + save.getPuzzle());
        }

        if (save.getPowerUsage() == null)
            throw new ValidationException("Solution must be solved");
        SzScore score = new SzScore(save.cost(), save.getPowerUsage(), save.lines());

        String title = save.getName().replace(" (Copy)", ""); // try to cut down on duplicate churn
        if (title.length() > 100)
            title = title.substring(0, 100) + "...";
        data = data.replaceFirst("^\n*\\[name] .*", "[name] " + title);

        return new SzSubmission(puzzle, score, author, displayLink, data);
    }
}
