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

package com.faendir.zachtronics.bot.sz.model;

import com.faendir.zachtronics.bot.model.Submission;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.regex.Pattern;

@Getter
public class SzSubmission implements Submission<SzCategory, SzPuzzle> {
    @Delegate
    private final SzSolutionMetadata solutionMetadata;
    private final String author;
    private final String displayLink;
    private final String data;
    @Setter
    private Path path;

    public SzSubmission(@NotNull String data) { // FIXME
        solutionMetadata = new SzSolutionMetadata(Pattern.compile("\r?\n").splitAsStream(data));
        this.data = data;
        this.author = null;
        this.displayLink = null;
        this.path = null;
    }

    public SzSubmission(@NotNull Path path) { // FIXME
        solutionMetadata = SzSolutionMetadata.fromPath(path);
        this.author = null;
        this.displayLink = null;
        this.data = null;
        this.path = path;
    }

    @Deprecated
    public SzRecord toRecord() {
        return new SzRecord(getPuzzle(), getScore(), author, displayLink, path);
    }
}
