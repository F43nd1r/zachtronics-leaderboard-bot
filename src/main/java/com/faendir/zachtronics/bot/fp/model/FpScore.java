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

package com.faendir.zachtronics.bot.fp.model;

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.Score;
import com.faendir.zachtronics.bot.model.StringFormat;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class FpScore implements Score<FpCategory> {
    int rules;
    int conditionalRules;
    int frames;
    int waste;

    /** rr/cr/ff/w */
    @NotNull
    @Override
    public String toDisplayString(@NotNull DisplayContext<FpCategory> context) {
        String separator = context.getSeparator();
        int formatId = 0b000;
        if (context.getFormat() == StringFormat.REDDIT && context.getCategories() != null) {
            formatId = context.getCategories().stream()
                              .map(FpCategory::getScoreFormatId)
                              .reduce((a, b) -> a & b)
                              .orElse(0b000);
        }

        return String.format(FpCategory.FORMAT_STRINGS[formatId], rules, separator, conditionalRules, separator, frames, separator, waste);
    }

    /** rr/cr/ff/w */
    private static final Pattern REGEX_SCORE = Pattern.compile(
            "\\**(?<rules>\\d+)\\**\\((?<conditionalRules>\\d+)\\)\\**/\\**(?<frames>\\d+)\\**/\\**(?<waste>\\d+)\\**");

    /** <tt>rr/cr/ff/w</tt>, tolerates extra <tt>*</tt> */
    @Nullable
    public static FpScore parseScore(@NotNull String string) {
        Matcher m = REGEX_SCORE.matcher(string);
        return m.matches() ? parseScore(m) : null;
    }

    /** we assume m matches */
    @NotNull
    public static FpScore parseScore(@NotNull Matcher m) {
        int rules = Integer.parseInt(m.group("rules"));
        int conditionalRules = Integer.parseInt(m.group("conditionalRules"));
        int frames = Integer.parseInt(m.group("frames"));
        int waste = Integer.parseInt(m.group("waste"));
        return new FpScore(rules, conditionalRules, frames, waste);
    }
}
