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

package com.faendir.zachtronics.bot.sz.model;

import lombok.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.regex.Pattern;

@Value
class SzSolutionMetadata {
    String title;
    SzPuzzle puzzle;
    SzScore score;

    @NotNull
    @Contract("_ -> new")
    public static SzSolutionMetadata fromData(@NotNull String data) {
        Iterator<String> it = Pattern.compile("\r?\n").splitAsStream(data).dropWhile(String::isBlank).iterator();
        /*
        [name] Top solution Cost->Power - andersk
        [puzzle] Sz040
        [production-cost] 1200
        [power-usage] 679
        [lines-of-code] 31
         */
        String nameLine = it.next();
        if (!nameLine.startsWith("[name]"))
            throw new IllegalArgumentException("Invalid solution file, first line: \"" + nameLine + "\"");

        String title = nameLine.replaceFirst("^\\[name] ", "").replace(" (Copy)", ""); // try to cut down on duplicate churn;
        SzPuzzle puzzle = SzPuzzle.valueOf(it.next().replaceFirst("^\\[puzzle] ", ""));

        String costLine = it.next();
        if (costLine.isBlank())
            throw new IllegalArgumentException("Solution must be solved");

        int cost = Integer.parseInt(costLine.replaceFirst("^\\[production-cost] ", "")) / 100;
        int power = Integer.parseInt(it.next().replaceFirst("^\\[power-usage] ", ""));
        int loc = Integer.parseInt(it.next().replaceFirst("^\\[lines-of-code] ", ""));
        SzScore score = new SzScore(cost, power, loc);

        return new SzSolutionMetadata(title, puzzle, score);
    }

    public SzSubmission extendToSubmission(@NotNull String author, String displayLink, @NotNull String data) {
        // TODO push the author at the end of the title
        data = data.replaceFirst("^\n*\\[name] .*", "[name] " + title);
        return new SzSubmission(puzzle, score, author, displayLink, data);
    }
}
