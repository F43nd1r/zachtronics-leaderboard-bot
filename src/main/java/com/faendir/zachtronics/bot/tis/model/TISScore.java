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

package com.faendir.zachtronics.bot.tis.model;

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.Score;
import com.faendir.zachtronics.bot.model.ScorePlainSerializer;
import com.faendir.zachtronics.bot.utils.Utils;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;
import lombok.With;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@JsonSerialize(using = ScorePlainSerializer.class)
public class TISScore implements Score<TISCategory> {
    @IntRange(from = 0) int cycles;
    @IntRange(from = 1, to = 4 * 3) int nodes;
    @IntRange(from = 1, to = 4 * 3 * 15) int instructions;

    @With boolean achievement;
    @With boolean cheating;
    /** implies {@link #cheating}{@code =true} */
    @With boolean hardcoded;

    /** ccc/nn/ii[/ach] */
    @NotNull
    @Override
    public String toDisplayString(@NotNull DisplayContext<TISCategory> context) {
        String separator = context.getSeparator();
        int formatId = Utils.getScoreFormatId(context);

        return String.format(TISCategory.FORMAT_STRINGS[formatId], cycles, separator, nodes, separator, instructions, sepFlags(separator));
    }

    /** ccc/nn/ii[/ach] */
    private static final Pattern REGEX_SCORE = Pattern.compile("\\**(?<cycles>\\d+)\\**[/-]" +
                                                               "\\**(?<nodes>\\d{1,2})\\**[/-]" +
                                                               "\\**(?<instructions>\\d{1,3})\\**" +
                                                               "(?:[/-](?<Aflag>[aA])?(?<Cflag>[cC])?(?<Hflag>[hH])?)?");

    /** <tt>ccc/nn/ii[/ach]</tt>, tolerates extra <tt>*</tt> */
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
        boolean cheating = m.group("Cflag") != null || m.group("Hflag") != null;
        return new TISScore(cycles, nodes, instructions, m.group("Aflag") != null, cheating, m.group("Hflag") != null);
    }

    public String sepFlags(String separator) {
        return sepFlags(separator, achievement, cheating, hardcoded);
    }

    /**
     * @return <tt>""</tt> or <tt>"/a"</tt> or <tt>"/c"</tt> or <tt>"/h"</tt> or <tt>"/ac"</tt> or <tt>"/ah"</tt>
     */
    @NotNull
    public static String sepFlags(String separator, boolean achievement, boolean cheating, boolean hardcoded) {
        if (achievement || cheating) {
            String result = separator;
            if (achievement) result += "a";
            if (cheating && !hardcoded) result += "c";
            if (hardcoded) result += "h";
            return result;
        }
        else return "";
    }
}

