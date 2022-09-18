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

package com.faendir.zachtronics.bot.cw.model;

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.Score;
import com.faendir.zachtronics.bot.utils.Utils;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class CwScore implements Score<CwCategory> {
    int width;
    int height;
    int footprint;

    /** W*H */
    public int getSize() {
        return width * height;
    }

    /** WxH/FF */
    @NotNull
    @Override
    public String toDisplayString(@NotNull DisplayContext<CwCategory> context) {
        String separator = context.getSeparator();
        int formatId = Utils.getScoreFormatId(context);

        return String.format(CwCategory.FORMAT_STRINGS[formatId], width, height, separator, footprint);
    }

    /** WxH/FF */
    private static final Pattern REGEX_SCORE = Pattern.compile(
            "\\**(?<width>\\d)x(?<height>\\d)\\**/\\**(?<footprint>\\d+)\\**F");

    /** <tt>WxH/FF</tt>, tolerates extra <tt>*</tt> */
    @Nullable
    public static CwScore parseScore(@NotNull String string) {
        Matcher m = REGEX_SCORE.matcher(string);
        return m.matches() ? parseScore(m) : null;
    }

    /** we assume m matches */
    @NotNull
    public static CwScore parseScore(@NotNull Matcher m) {
        int width = Integer.parseInt(m.group("width"));
        int height = Integer.parseInt(m.group("height"));
        int footprint = Integer.parseInt(m.group("footprint"));
        return new CwScore(width, height, footprint);
    }
}
