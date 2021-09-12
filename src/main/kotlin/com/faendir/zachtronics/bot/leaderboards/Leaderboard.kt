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

package com.faendir.zachtronics.bot.leaderboards

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record

interface Leaderboard<C : Category, P : Puzzle, R : Record> {
    val supportedCategories: List<C>

    fun update(puzzle: P, record: R): UpdateResult = UpdateResult.NotSupported()

    fun get(puzzle: P, category: C): R?

    fun getAll(puzzle: P, categories: Collection<C>): Map<C, R>
}

sealed class UpdateResult {
    class Success(val oldRecords: Map<out Category, Record?>) : UpdateResult()

    class ParetoUpdate : UpdateResult()

    class BetterExists(val records: Map<out Category, Record>) : UpdateResult()

    class NotSupported : UpdateResult()
}

