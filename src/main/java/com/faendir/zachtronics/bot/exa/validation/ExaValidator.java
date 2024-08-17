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

package com.faendir.zachtronics.bot.exa.validation;

import com.faendir.zachtronics.bot.exa.model.ExaPuzzle;
import com.faendir.zachtronics.bot.exa.model.ExaScore;
import com.faendir.zachtronics.bot.exa.model.ExaSubmission;
import com.faendir.zachtronics.bot.validation.ValidationException;
import org.jetbrains.annotations.NotNull;

public class ExaValidator {
    private ExaValidator() {}

    public static @NotNull ExaSubmission validate(byte[] data, boolean cheesy, String author, String displayLink) {
        ExaSave save = ExaSave.unmarshal(data);

        ExaPuzzle puzzle;
        try {
            puzzle = ExaPuzzle.valueOf(save.getPuzzle());
        }
        catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown puzzle: " + save.getPuzzle());
        }

        if (save.getSize() > puzzle.getSizeLimit())
            throw new ValidationException("Size larger than puzzle limit: " + save.getSize() + " > " + puzzle.getSizeLimit());
        int actualSize = save.actualSize();
        if (save.getSize() != actualSize)
            throw new ValidationException("Actual size different from declared: " + actualSize + " != " + save.getSize());

        ExaScore score = new ExaScore(save.getCycles(), save.getSize(), save.getActivity(), cheesy);
        return new ExaSubmission(puzzle, score, author, displayLink, data);
    }
}
