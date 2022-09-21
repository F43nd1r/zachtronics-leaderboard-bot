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

package com.faendir.zachtronics.bot.fc.model;

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.Score;
import com.faendir.zachtronics.bot.utils.Utils;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class FcScore implements Score<FcCategory> {
    int time;
    int cost;
    int sumTimes;
    int wires;

    /** tT/ck/sS/wW */
    @NotNull
    @Override
    public String toDisplayString(@NotNull DisplayContext<FcCategory> context) {
        String separator = context.getSeparator();
        int formatId = Utils.getScoreFormatId(context);

        return String.format(FcCategory.FORMAT_STRINGS[formatId], time, separator, cost, separator, sumTimes, separator, wires);
    }

    /** tT/ck/sS/wW */
    private static final Pattern REGEX_SCORE = Pattern.compile(
            "\\**(?<time>\\d+)\\**T/\\**(?<cost>\\d+)\\**k/\\**(?<sumTimes>\\d+)\\**S/\\**(?<wires>\\d+)\\**W");

    /** <tt>tT/ck/sS/wW</tt>, tolerates extra <tt>*</tt> */
    @Nullable
    public static FcScore parseScore(@NotNull String string) {
        Matcher m = REGEX_SCORE.matcher(string);
        return m.matches() ? parseScore(m) : null;
    }

    /** we assume m matches */
    @NotNull
    public static FcScore parseScore(@NotNull Matcher m) {
        int rules = Integer.parseInt(m.group("time"));
        int conditionalRules = Integer.parseInt(m.group("cost"));
        int frames = Integer.parseInt(m.group("sumTimes"));
        int waste = Integer.parseInt(m.group("wires"));
        return new FcScore(rules, conditionalRules, frames, waste);
    }
}
