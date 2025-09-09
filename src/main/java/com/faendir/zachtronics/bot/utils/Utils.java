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

package com.faendir.zachtronics.bot.utils;

import com.faendir.zachtronics.bot.model.CategoryJava;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.StringFormat;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.ContentDisposition;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {
    private Utils() {}

    private static final Pattern PASTEBIN_PATTERN = Pattern.compile("(?:https?://)?pastebin.com/(?:(?:raw|dl)/)?(\\w+)");
    private static final Pattern GDOCS_PATTERN = Pattern.compile("(?:https?://)?docs.google.com/document/d/([\\w-]+)(?:/.*)?");
    @NotNull
    public static String rawContentURL(@NotNull String link) {
        Matcher m = PASTEBIN_PATTERN.matcher(link);
        if (m.matches()) { // pastebin has an easy way to get raw text
            return "https://pastebin.com/raw/" + m.group(1);
        }

        m = GDOCS_PATTERN.matcher(link);
        if (m.matches()) { // gdocs can export to text from url
            return "https://docs.google.com/document/d/" + m.group(1) + "/export?format=txt";
        }

        return link;
    }

    @Value
    public static class FileInfo {
        @Nullable String filename;
        byte @NotNull [] data;

        public @NotNull String dataAsString() {
            String content = new String(data).replace("\r\n", "\n");
            content = content.replaceFirst("\\s*$", "\n"); // ensure there is one and only one newline at the end
            if (!content.isEmpty() && content.charAt(0) == '\uFEFF') // remove BOM
                content = content.substring(1);
            return content;
        }
    }

    public static @NotNull FileInfo downloadFile(@NotNull String link) {
        try {
            URLConnection connection = new URI(Utils.rawContentURL(link)).toURL().openConnection();
            try (InputStream is = connection.getInputStream()) { // we're connected now
                String filename = null;
                String cdHeader = connection.getHeaderField("content-disposition");
                if (cdHeader != null)
                    filename = ContentDisposition.parse(cdHeader).getFilename();
                return new FileInfo(filename, is.readAllBytes());
            }
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException("Could not parse your link");
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read your solution");
        }
    }

    @NotNull
    public static <T extends Enum<T>> EnumSet<T> enumSetUnion(EnumSet<T> s1, EnumSet<T> s2) {
        EnumSet<T> res = EnumSet.copyOf(s1);
        res.addAll(s2);
        return res;
    }

    public static <Cat extends CategoryJava<?, ?, ?>> int getScoreFormatId(@NotNull DisplayContext<Cat> context) {
        int formatId = 0b000;
        if (context.getFormat() == StringFormat.REDDIT && context.getCategories() != null) {
            formatId = context.getCategories().stream()
                              .map(Cat::getScoreFormatId)
                              .reduce((a, b) -> a & b)
                              .orElse(0b000);
        }
        return formatId;
    }
}
