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

package com.faendir.zachtronics.bot.kz.model;

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.Score;
import com.faendir.zachtronics.bot.utils.Utils;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class KzScore implements Score<KzCategory> {
    int time;
    int cost;
    int area;

    /** tt/cc/aa */
    @NotNull
    @Override
    public String toDisplayString(@NotNull DisplayContext<KzCategory> context) {
        String separator = context.getSeparator();
        int formatId = Utils.getScoreFormatId(context);

        return String.format(KzCategory.FORMAT_STRINGS[formatId], time, separator, cost, separator, area);
    }

    /** tt/cc/aa */
    private static final Pattern REGEX_SCORE = Pattern.compile(
            "\\**(?<time>\\d+)\\**/\\**(?<cost>\\d+)\\**/\\**(?<area>\\d+)\\**");

    /** <tt>tt/cc/aa</tt>, tolerates extra <tt>*</tt> */
    @Nullable
    public static KzScore parseScore(@NotNull String string) {
        Matcher m = REGEX_SCORE.matcher(string);
        return m.matches() ? parseScore(m) : null;
    }

    /** we assume m matches */
    @NotNull
    public static KzScore parseScore(@NotNull Matcher m) {
        int time = Integer.parseInt(m.group("time"));
        int cost = Integer.parseInt(m.group("cost"));
        int area = Integer.parseInt(m.group("area"));
        return new KzScore(time, cost, area);
    }
}
