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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

@Value
class SzSolutionMetadata {
    String title;
    SzPuzzle puzzle;
    SzScore score;

    @NotNull
    @Contract("_ -> new")
    public static SzSolutionMetadata fromPath(@NotNull Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            return new SzSolutionMetadata(lines);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Closing the stream (if needed) is the caller's issue */
    SzSolutionMetadata(@NotNull Stream<String> lines) {
        Iterator<String> it = lines.iterator();
        /*
        [name] Top solution Cost->Power - andersk
        [puzzle] Sz040
        [production-cost] 1200
        [power-usage] 679
        [lines-of-code] 31
         */
        title = it.next().replaceFirst("^.+] ", "");
        puzzle = SzPuzzle.valueOf(it.next().replaceFirst("^.+] ", ""));
        int cost = Integer.parseInt(it.next().replaceFirst("^.+] ", "")) / 100;
        int power = Integer.parseInt(it.next().replaceFirst("^.+] ", ""));
        int loc = Integer.parseInt(it.next().replaceFirst("^.+] ", ""));
        score = new SzScore(cost, power, loc);
    }
}
