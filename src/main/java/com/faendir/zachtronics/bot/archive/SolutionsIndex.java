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

package com.faendir.zachtronics.bot.archive;

import com.faendir.zachtronics.bot.model.Score;
import com.faendir.zachtronics.bot.model.Solution;
import kotlin.Pair;

import java.io.IOException;
import java.util.Collection;

/** Interface for classes that handle archival data for a level in the respective repository */
public interface SolutionsIndex<S extends Solution<?>> {

    /**
     * @return whether the new solution sits on the frontier
     */
    boolean add(S solution) throws IOException;

    /**
     * @return <tt>[(score, filename), ...]</tt>, if there's no associated file <tt>filename</tt> is null
     */
    Collection<Pair<Score, String>> getAll();
}
