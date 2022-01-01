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
import com.faendir.zachtronics.bot.model.Record;
import com.faendir.zachtronics.bot.utils.Markdown;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Value
@Builder
public class ScRecord implements Record<ScCategory> {
    public static final ScRecord IMPOSSIBLE_CATEGORY = new ScRecord(ScPuzzle.bonding_boss, ScScore.INVALID_SCORE,
                                                                    null, null, false, null, null);

    @NotNull ScPuzzle puzzle;
    @NotNull ScScore score;
    String author;
    /** not-<tt>null</tt> IFF the solution holds a category */
    String displayLink;
    boolean oldVideoRNG;
    String dataLink;
    Path dataPath;

    @NotNull
    @Override
    public String toDisplayString(@NotNull DisplayContext<ScCategory> context) {
        return toDisplayString(context, "");
    }

    /**
     * @param reactorPrefix will be put between the archive and the video link
     */
    @NotNull
    public String toDisplayString(@NotNull DisplayContext<ScCategory> context, String reactorPrefix) {
        String scoreAuthor = "(" + score.toDisplayString(context, oldVideoRNG ? "\\*" : "") + ")" +
                             (author != null ? " " + author : "");
        return Markdown.fileLinkOrEmpty(dataLink) +
               reactorPrefix + Markdown.linkOrText(scoreAuthor, displayLink, true);
    }
}