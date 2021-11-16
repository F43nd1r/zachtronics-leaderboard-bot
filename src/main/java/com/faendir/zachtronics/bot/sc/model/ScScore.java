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

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.Score;
import com.faendir.zachtronics.bot.model.StringFormat;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Value
public class ScScore implements Score<ScCategory> {
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
    public String toDisplayString(@NotNull DisplayContext<ScCategory> context) {
        return toDisplayString(context, "");
    }

    /** ccc{}/r/ss[/BP] */
    @NotNull
    public String toDisplayString(@NotNull DisplayContext<ScCategory> context, String oldRNGMarker) {
        String separator = context.getSeparator();
        String cyclesStr = cycles >= 100000 ? NumberFormat.getNumberInstance(Locale.ROOT).format(cycles)
                                            : Integer.toString(cycles);
        String formatString = ScCategory.ScScoreFormatStrings.F000;
        if (context.getFormat() == StringFormat.REDDIT && context.getCategories() != null) {
            Set<String> formatStrings = context.getCategories().stream()
                                               .map(ScCategory::getScoreFormatString)
                                               .collect(Collectors.toSet());
            if (formatStrings.size() == 1)
                formatString = formatStrings.iterator().next();
        }

        return String.format(formatString,
                             cyclesStr, oldRNGMarker, separator, reactors, separator, symbols, sepFlags(separator));
    }

    /** <tt>ccc/r/ss[/BP]</tt>, tolerates extra <tt>*</tt> */
    public static final Pattern REGEX_BP_SCORE = Pattern.compile(
            "\\**(?<cycles>[\\d,]+)\\**(?<oldRNG>\\\\\\*)?[/-]" +
            "\\**(?<reactors>\\d+)\\**[/-]" +
            "\\**(?<symbols>\\d+)\\**" +
            "(?:[/-](?<Bflag>B)?(?<Pflag>P)?)?");

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

        return new ScScore(cycles, reactors, symbols,
                           m.group("Bflag") != null, m.group("Pflag") != null);
    }

    public String sepFlags(String separator) {
        return sepFlags(separator, bugged, precognitive);
    }

    /**
     * @return <tt>""</tt> or <tt>"/B"</tt> or <tt>"/P"</tt> or <tt>"/BP"</tt>
     */
    public static String sepFlags(String separator, boolean bugged, boolean precognitive) {
        if (bugged || precognitive) {
            String result = String.valueOf(separator);
            if (bugged) result += "B";
            if (precognitive) result += "P";
            return result;
        }
        else return "";
    }
}
