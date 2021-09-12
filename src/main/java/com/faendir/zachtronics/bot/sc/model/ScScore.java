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

package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.Score;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class ScScore implements Score {
    /** it's also unbeatable */
    public static final ScScore INVALID_SCORE = new ScScore(-1, -1, -1, false, false);

    int cycles;
    int reactors;
    int symbols;

    boolean bugged;
    boolean precognitive;

    /** ccc/r/ss[/BP] */
    @NotNull
    @Override
    public String toDisplayString() {
        return cycles + "/" + reactors + "/" + symbols + slashFlags();
    }

    /** <tt>ccc/r/ss[/BP]</tt>, tolerates extra <tt>*</tt> */
    public static final Pattern REGEX_BP_SCORE = Pattern.compile(
            "\\**(?<cycles>[\\d,]+)\\**(?<oldRNG>\\\\\\*)?[/-]" +
                    "\\**(?<reactors>\\d+)\\**[/-]" +
                    "\\**(?<symbols>\\d+)\\**" +
                    "(?:[/-](?<flags>B?P?))?");

    /** <tt>ccc/r/ss[/BP]</tt>, tolerates extra <tt>*</tt> */
    @Nullable
    public static ScScore parseBPScore(@NotNull String string) {
        Matcher m = REGEX_BP_SCORE.matcher(string);
        return m.matches() ? parseBPScore(m) : null;
    }

    /** we assume m matches */
    @NotNull
    public static ScScore parseBPScore(@NotNull Matcher m) {
        int cycles = Integer.parseInt(m.group("cycles").replace(",", ""));
        int reactors = Integer.parseInt(m.group("reactors"));
        int symbols = Integer.parseInt(m.group("symbols"));

        String flags = m.group("flags");

        return new ScScore(cycles, reactors, symbols,
                flags != null && flags.contains("B"),
                flags != null && flags.contains("P"));
    }

    /**
     * @return <tt>""</tt> or <tt>"/B"</tt> or <tt>"/P"</tt> or <tt>"/BP"</tt>
     */
    public String slashFlags() {
        return slashFlags(bugged, precognitive);
    }

    /**
     * @return <tt>""</tt> or <tt>"/B"</tt> or <tt>"/P"</tt> or <tt>"/BP"</tt>
     */
    public static String slashFlags(boolean bugged, boolean precognitive) {
        if (bugged || precognitive) {
            String result = "/";
            if (bugged) result += "B";
            if (precognitive) result += "P";
            return result;
        } else return "";
    }
}
