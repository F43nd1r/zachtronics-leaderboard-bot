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
        Iterator<String> it = Pattern.compile("\n").splitAsStream(data).dropWhile(String::isBlank).iterator();
        /*
        [name] Top solution Cost->Power - andersk
        [puzzle] Sz040
        [production-cost] 1200
        [power-usage] 679
        [lines-of-code] 31
         */
        String firstLine = it.next();
        if (!firstLine.startsWith("[name]"))
            throw new IllegalArgumentException("Invalid solution file, first line: \"" + firstLine + "\"");

        String title = firstLine.replaceFirst("^.+] ", "");
        SzPuzzle puzzle = SzPuzzle.valueOf(it.next().replaceFirst("^.+] ", ""));
        int cost = Integer.parseInt(it.next().replaceFirst("^.+] ", "")) / 100;
        int power = Integer.parseInt(it.next().replaceFirst("^.+] ", ""));
        int loc = Integer.parseInt(it.next().replaceFirst("^.+] ", ""));
        SzScore score = new SzScore(cost, power, loc);

        return new SzSolutionMetadata(title, puzzle, score);
    }

    public SzSubmission extendToSubmission(@NotNull String author, @NotNull String data) {
        // TODO push the author at the end of the title
        return new SzSubmission(puzzle, score, author, data);
    }
}
