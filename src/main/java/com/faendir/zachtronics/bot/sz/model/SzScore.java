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

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.Score;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class SzScore implements Score<SzCategory> {
    int cost;
    int power;
    int lines;

    /** ccc/r/ss */
    @NotNull
    @Override
    public String toDisplayString(@NotNull DisplayContext<SzCategory> context) {
        return String.valueOf(cost) + context.getSeparator() + power + context.getSeparator() + lines;
    }

    /** cc/ppp/ll */
    public static final Pattern REGEX_SIMPLE_SCORE = Pattern.compile(
            "\\**(?<cost>\\d+)\\**/\\**(?<power>\\d+)\\**/\\**(?<lines>\\d+)\\**");

    /** we assume m matches */
    @NotNull
    public static SzScore parseSimpleScore(@NotNull Matcher m) {
        int cost = Integer.parseInt(m.group("cost"));
        int power = Integer.parseInt(m.group("power"));
        int lines = Integer.parseInt(m.group("lines"));
        return new SzScore(cost, power, lines);
    }
}
