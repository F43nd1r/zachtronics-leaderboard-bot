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

package com.faendir.zachtronics.bot.exa.model;

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.Score;
import com.faendir.zachtronics.bot.utils.Utils;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class ExaScore implements Score<ExaCategory> {
    int cycles;
    int size;
    int activity;

    @With boolean cheesy;

    /** cc/ss/aa[/c] */
    @NotNull
    @Override
    public String toDisplayString(@NotNull DisplayContext<ExaCategory> context) {
        String separator = context.getSeparator();
        int formatId = Utils.getScoreFormatId(context);

        return String.format(ExaCategory.FORMAT_STRINGS[formatId], cycles, separator, size, separator, activity, sepFlags(separator));
    }

    /** cc/ss/aa[/c] */
    private static final Pattern REGEX_SCORE = Pattern.compile("\\**(?<cycles>\\d+)\\**[/-]" +
                                                               "\\**(?<size>\\d+)\\**[/-]" +
                                                               "\\**(?<activity>\\d+)\\**" +
                                                               "(?:[/-](?<Cflag>[cC]))?");

    /** <tt>cc/ss/aa[/c]</tt>, tolerates extra <tt>*</tt> */
    @Nullable
    public static ExaScore parseScore(@NotNull String string) {
        Matcher m = REGEX_SCORE.matcher(string);
        return m.matches() ? parseScore(m) : null;
    }

    /** we assume m matches */
    @NotNull
    public static ExaScore parseScore(@NotNull Matcher m) {
        int cycles = Integer.parseInt(m.group("cycles"));
        int size = Integer.parseInt(m.group("size"));
        int activity = Integer.parseInt(m.group("activity"));
        return new ExaScore(cycles, size, activity, m.group("Cflag") != null);
    }

    public String sepFlags(String separator) {
        return sepFlags(separator, cheesy);
    }

    /**
     * @return <tt>""</tt> or <tt>"/c"</tt>
     */
    @NotNull
    public static String sepFlags(String separator, boolean cheesy) {
        return cheesy ? separator + "c" : "";
    }
}
