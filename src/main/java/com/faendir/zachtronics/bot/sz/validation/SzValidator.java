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

package com.faendir.zachtronics.bot.sz.validation;

import com.faendir.zachtronics.bot.sz.model.SzScore;
import com.faendir.zachtronics.bot.sz.model.SzSubmission;
import com.faendir.zachtronics.bot.validation.ValidationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SzValidator {
    private SzValidator() {}

    public static SzSubmission validate(String data, String author, String displayLink) {
        SzSave save = SzSave.unmarshal(data);

        if (save.getPowerUsage() == null)
            throw new ValidationException("Solution must be solved");
        SzScore score = new SzScore(save.cost(), save.getPowerUsage(), save.lines());

        String title = save.getName().replace(" (Copy)", ""); // try to cut down on duplicate churn
        data = data.replaceFirst("^\n*\\[name] .*", "[name] " + title);

        return new SzSubmission(save.getPuzzle(), score, author, displayLink, data);
    }

    @NotNull
    static String readTag(@NotNull String block, @NotNull String tag) {
        if (block.startsWith("[" + tag + "]")) {
            int headerLength = tag.length() + 2;
            return block.substring(headerLength).stripLeading();
        }
        else
            throw new ValidationException("No [" + tag + "] in \"" + block + "\"");
    }

    @NotNull
    public static Map<String, String> readAllTags(@NotNull String lines) {
        String[] blocks = Pattern.compile("\\n(?:(?=\\[)|$)").split(lines);
        Map<String, String> result = new HashMap<>();
        for (String block : blocks) {
            String tag = block.substring(1, block.indexOf(']'));
            String content = block.substring(block.indexOf(']') + 1).stripLeading();
            result.put(tag, content);
        }
        return result;
    }

    /** absent = exception */
    public static int getInt(@NotNull Map<String, String> map, String tag) {
        String candidate = map.get(tag);
        if (candidate != null)
            return Integer.parseInt(candidate);
        else
            throw new ValidationException("No [" + tag + "] in map");
    }

    /** absent = exception */
    @Nullable
    public static Integer getIntOrNull(@NotNull Map<String, String> map, String tag) {
        String candidate = map.get(tag);
        return candidate == null ? null : Integer.valueOf(candidate);
    }

    /** absent = false */
    public static boolean getBoolean(@NotNull Map<String, String> map, String tag) {
        return Boolean.parseBoolean(map.get(tag));
    }
}
