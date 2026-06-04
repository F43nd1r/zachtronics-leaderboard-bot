/*
 * Copyright (c) 2026
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
import com.faendir.zachtronics.bot.model.Record;
import com.faendir.zachtronics.bot.utils.Markdown;
import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

@Value
public class ScRecord implements Record<ScCategory> {
    ScPuzzle puzzle;
    ScScore score;
    String author;
    @Nullable String displayLink;
    /** <tt>null</tt> if the solution holds a video spot but has since been out-pareto'd */
    @Nullable String dataLink;
    /** <tt>null</tt> if the solution holds a video spot but has since been out-pareto'd */
    @Nullable Path dataPath;

    @Override
    public String toDisplayString(DisplayContext<ScCategory> context) {
        return toDisplayString(context, "");
    }

    /**
     * @param reactorPrefix will be put between the archive and the video link
     */
    public String toDisplayString(DisplayContext<ScCategory> context, String reactorPrefix) {
        String scoreAuthor = "(" + score.toDisplayString(context) + ") " + Markdown.escape(author);
        return Markdown.fileLinkOrEmpty(dataLink) +
               reactorPrefix + Markdown.linkOrText(scoreAuthor, displayLink);
    }
}
