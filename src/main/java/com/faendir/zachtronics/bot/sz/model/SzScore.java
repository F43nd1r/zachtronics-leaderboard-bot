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

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.Score;
import com.faendir.zachtronics.bot.utils.Utils;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class SzScore implements Score<SzCategory> {
    int cost;
    int power;
    int lines;

    /** cc/ppp/ll */
    @NotNull
    @Override
    public String toDisplayString(@NotNull DisplayContext<SzCategory> context) {
        String separator = context.getSeparator();
        int formatId = Utils.getScoreFormatId(context);

        return String.format(SzCategory.FORMAT_STRINGS[formatId], cost, separator, power, separator, lines);
    }

    /** cc/ppp/ll */
    private static final Pattern REGEX_SCORE = Pattern.compile(
            "\\**(?<cost>\\d+)\\**/\\**(?<power>\\d+)\\**/\\**(?<lines>\\d+)\\**");

    /** <tt>cc/ppp/ll</tt>, tolerates extra <tt>*</tt> */
    @Nullable
    public static SzScore parseScore(@NotNull String string) {
        Matcher m = REGEX_SCORE.matcher(string);
        return m.matches() ? parseScore(m) : null;
    }

    /** we assume m matches */
    @NotNull
    public static SzScore parseScore(@NotNull Matcher m) {
        int cost = Integer.parseInt(m.group("cost"));
        int power = Integer.parseInt(m.group("power"));
        int lines = Integer.parseInt(m.group("lines"));
        return new SzScore(cost, power, lines);
    }
}
