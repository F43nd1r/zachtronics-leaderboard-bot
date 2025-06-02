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

package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.Score;
import com.faendir.zachtronics.bot.model.ScorePlainSerializer;
import com.faendir.zachtronics.bot.model.StringFormat;
import com.faendir.zachtronics.bot.utils.Utils;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@JsonSerialize(using = ScorePlainSerializer.class)
public class ScScore implements Score<ScCategory> {
    int cycles;
    int reactors;
    int symbols;

    boolean bugged;
    boolean precognitive;

    /** ccc/r/ss[/BP] */
    @NotNull
    @Override
    public String toDisplayString(@NotNull DisplayContext<ScCategory> context) {
        String separator = context.getSeparator();
        String cyclesStr = context.getFormat() != StringFormat.FILE_NAME && cycles >= 100000 ?
                           NumberFormat.getNumberInstance(Locale.ROOT).format(cycles) :
                           Integer.toString(cycles);
        int formatId = Utils.getScoreFormatId(context);

        return String.format(ScCategory.FORMAT_STRINGS[formatId],
                             cyclesStr, separator, reactors, separator, symbols, sepFlags(separator));
    }

    /** </tt>ccc-r-ss,[/BP]</tt>, includes comma in any case */
    @NotNull
    public String toExportString() {
        return String.format("%d-%d-%d,%s", cycles, reactors, symbols, sepFlags("/"));
    }

    /** <tt>ccc/r/ss[/BP]</tt>, tolerates extra <tt>*</tt> */
    private static final Pattern REGEX_BP_SCORE = Pattern.compile(
            "\\**(?<cycles>[\\d,]+)\\**[/-]" +
            "\\**(?<reactors>\\d+)\\**[/-]" +
            "\\**(?<symbols>\\d+)\\**" +
            "(?:[/-](?<Bflag>[bB])?(?<Pflag>[pP])?)?");

    /** <tt>ccc/r/ss[/BP]</tt>, tolerates extra <tt>*</tt> */
    @Nullable
    public static ScScore parseScore(@NotNull String string) {
        Matcher m = REGEX_BP_SCORE.matcher(string);
        return m.matches() ? parseScore(m) : null;
    }

    /** we assume m matches */
    @NotNull
    public static ScScore parseScore(@NotNull Matcher m) {
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
            String result = separator;
            if (bugged) result += "B";
            if (precognitive) result += "P";
            return result;
        }
        else return "";
    }
}
