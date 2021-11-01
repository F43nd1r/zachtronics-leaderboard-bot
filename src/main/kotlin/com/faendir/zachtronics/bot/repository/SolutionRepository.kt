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

package com.faendir.zachtronics.bot.repository

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.model.Submission

interface SolutionRepository<C : Category, P : Puzzle, S : Submission<C, P>, R: Record<C>> {
    fun submit(submission: S) : SubmitResult<R, C>

    fun submitAll(submissions: Collection<S>): List<SubmitResult<R, C>> = submissions.map { submit(it) }

    fun find(puzzle: P, category: C) : R?

    fun findCategoryHolders(puzzle: P, includeFrontier: Boolean) : List<CategoryRecord<R, C>>
}

sealed class SubmitResult<R: Record<C>, C: Category> {
    class Success<R: Record<C>, C: Category>(val message: String?, val beatenRecords: Collection<CategoryRecord<R?, C>>) : SubmitResult<R, C>()
    class AlreadyPresent<R: Record<C>, C: Category> : SubmitResult<R, C>()
    class NothingBeaten<R: Record<C>, C: Category>(val records: Collection<CategoryRecord<R, C>>) : SubmitResult<R, C>()
    class Failure<R: Record<C>, C: Category>(val message: String): SubmitResult<R, C>()
}

data class CategoryRecord<R: Record<C>?, C: Category>(val record: R, val categories: Set<C>)