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

package com.faendir.zachtronics.bot.inf.model;

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.Score;
import com.faendir.zachtronics.bot.utils.Utils;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class IfScore implements Score<IfCategory> {
    int cycles;
    int footprint;
    int blocks;

    @Accessors(fluent = true)
    boolean usesGRA;
    boolean finite;

    /** ccc/ff/bb[/GF] */
    @NotNull
    @Override
    public String toDisplayString(@NotNull DisplayContext<IfCategory> context) {
        String separator = context.getSeparator();
        int formatId = Utils.getScoreFormatId(context);

        return String.format(IfCategory.FORMAT_STRINGS[formatId], cycles, separator, footprint, separator, blocks, sepFlags(separator))
                     .replace("9999", "?"); // TODO dreadful hack for unknown values encoded as 9999 in the backend
    }

    /** ccc/ff/bb[/GF] */
    private static final Pattern REGEX_SCORE = Pattern.compile("\\**(?<cycles>\\d+)\\**[/-]" +
                                                               "\\**(?<footprint>\\d+)\\**[/-]" +
                                                               "\\**(?<blocks>\\d+)\\**" +
                                                               "(?:[/-](?<GRAflag>[gG])?(?<Fflag>[fF])?)?");

    /** <tt>ccc/ff/bb[/GF]</tt>, tolerates extra <tt>*</tt> */
    @Nullable
    public static IfScore parseScore(@NotNull String string) {
        Matcher m = REGEX_SCORE.matcher(string);
        return m.matches() ? parseScore(m) : null;
    }

    /** we assume m matches */
    @NotNull
    public static IfScore parseScore(@NotNull Matcher m) {
        int cycles = Integer.parseInt(m.group("cycles"));
        int footprint = Integer.parseInt(m.group("footprint"));
        int blocks = Integer.parseInt(m.group("blocks"));
        return new IfScore(cycles, footprint, blocks, m.group("GRAflag") != null, m.group("Fflag") != null);
    }

    public String sepFlags(String separator) {
        return sepFlags(separator, usesGRA, finite);
    }

    /**
     * @return <tt>""</tt> or <tt>"/G"</tt> or <tt>"/F"</tt> or <tt>"/GF"</tt>
     */
    @NotNull
    public static String sepFlags(String separator, boolean usesGRA, boolean finite) {
        if (usesGRA || finite) {
            String result = separator;
            if (usesGRA) result += "G";
            if (finite) result += "F";
            return result;
        }
        else
            return "";
    }
}
