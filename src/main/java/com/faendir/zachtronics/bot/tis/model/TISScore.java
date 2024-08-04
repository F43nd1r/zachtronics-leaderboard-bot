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

package com.faendir.zachtronics.bot.tis.model;

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.Score;
import com.faendir.zachtronics.bot.utils.Utils;
import lombok.Value;
import lombok.With;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class TISScore implements Score<TISCategory> {
    @IntRange(from = 0) int cycles;
    @IntRange(from = 1, to = 4 * 3) int nodes;
    @IntRange(from = 1, to = 4 * 3 * 15) int instructions;

    @With boolean achievement;
    @With boolean cheating;

    /** ccc/nn/ii[/ac] */
    @NotNull
    @Override
    public String toDisplayString(@NotNull DisplayContext<TISCategory> context) {
        String separator = context.getSeparator();
        int formatId = Utils.getScoreFormatId(context);

        return String.format(TISCategory.FORMAT_STRINGS[formatId], cycles, separator, nodes, separator, instructions, sepFlags(separator));
    }

    /** a?c? */
    public static final String FLAGS_REGEX = "(?<Aflag>[aA])?(?<Cflag>[cC])?";
    /** ccc/nn/ii[/ac] */
    private static final Pattern REGEX_SCORE = Pattern.compile("\\**(?<cycles>\\d+)\\**[/-]" +
                                                               "\\**(?<nodes>\\d{1,2})\\**[/-]" +
                                                               "\\**(?<instructions>\\d{1,3})\\**" +
                                                               "(?:[/-]" + FLAGS_REGEX + ")?");

    /** <tt>ccc/nn/ii[/ac]</tt>, tolerates extra <tt>*</tt> */
    @Nullable
    public static TISScore parseScore(@NotNull String string) {
        Matcher m = REGEX_SCORE.matcher(string);
        return m.matches() ? parseScore(m) : null;
    }

    /** we assume m matches */
    @NotNull
    public static TISScore parseScore(@NotNull Matcher m) {
        int cycles = Integer.parseInt(m.group("cycles"));
        int nodes = Integer.parseInt(m.group("nodes"));
        int instructions = Integer.parseInt(m.group("instructions"));
        return new TISScore(cycles, nodes, instructions, m.group("Aflag") != null, m.group("Cflag") != null);
    }

    public String sepFlags(String separator) {
        return sepFlags(separator, achievement, cheating);
    }

    /**
     * @return <tt>""</tt> or <tt>"/a"</tt> or <tt>"/c"</tt> or <tt>"/ac"</tt>
     */
    @NotNull
    public static String sepFlags(String separator, boolean achievement, boolean cheating) {
        if (achievement || cheating) {
            String result = separator;
            if (achievement) result += "a";
            if (cheating) result += "c";
            return result;
        }
        else return "";
    }
}
