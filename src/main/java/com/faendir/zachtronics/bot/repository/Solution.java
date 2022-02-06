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

package com.faendir.zachtronics.bot.repository;

import com.faendir.zachtronics.bot.model.Category;
import com.faendir.zachtronics.bot.model.Puzzle;
import com.faendir.zachtronics.bot.model.Record;
import com.faendir.zachtronics.bot.model.Score;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.EnumSet;

public interface Solution<C extends Enum<C> & Category, P extends Puzzle<C>, S extends Score<C>, R extends Record<C>> {
    @NotNull S getScore();
    String getAuthor();
    @NotNull EnumSet<C> getCategories();

    CategoryRecord<R, C> extendToCategoryRecord(P puzzle, String dataLink, Path dataPath);

    @NotNull
    String[] marshal();
}
