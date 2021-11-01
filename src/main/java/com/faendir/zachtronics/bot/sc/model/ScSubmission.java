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

import com.faendir.zachtronics.bot.model.Submission;
import com.faendir.zachtronics.bot.sc.validator.SChem;
import com.faendir.zachtronics.bot.utils.Utils;
import lombok.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/** Archive-only submissions have a <tt>null</tt> {@link #displayLink} */
@Value
public class ScSubmission implements Submission<ScCategory, ScPuzzle> {
    @NotNull ScPuzzle puzzle;
    @NotNull ScScore score;
    @NotNull String author;
    String displayLink;
    @NotNull String data;

    /**
     * @throws IllegalArgumentException if we can't correctly parse metadata
     */
    @NotNull
    public static ScSubmission fromDataNoValidation(@NotNull String data, @Nullable ScPuzzle puzzle) throws IllegalArgumentException {
        ScSolutionMetadata metadata = ScSolutionMetadata.fromHeader(data, puzzle);
        return metadata.extendToSubmission(null, data);
    }

    @NotNull
    public static List<ScSubmission> fromExportLink(@NotNull String exportLink, ScPuzzle puzzle, boolean bypassValidation) {
        String export;
        try (InputStream is = new URL(Utils.rawContentURL(exportLink)).openStream()) {
            export = new String(is.readAllBytes()).replace("\r\n", "\n");
            if (export.length() > 0 && export.charAt(0) == '\uFEFF') // remove BOM
                export = export.substring(1);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not parse your link");
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read your solution");
        }

        return SChem.validateMultiExport(export, puzzle, bypassValidation);
    }

    /** <tt>{{@link ScPuzzle#bonding_boss}, {@link ScScore#INVALID_SCORE}, "", null, "$reason"}</tt> */
    @NotNull
    @Contract("_ -> new")
    public static ScSubmission invalidSubmission(String reason) {
        return new ScSubmission(ScPuzzle.bonding_boss, ScScore.INVALID_SCORE, "", null, reason);
    }

    /** If returns <tt>false</tt>, the reason is in {@link #data} */
    public boolean isValid() {
        return score != ScScore.INVALID_SCORE;
    }

    public ScRecord extendToRecord(String dataLink, Path dataPath) {
        return new ScRecord(puzzle, score, author, displayLink, false, dataLink, dataPath);
    }
}
