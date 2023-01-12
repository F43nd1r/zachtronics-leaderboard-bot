/*
 * Copyright (c) 2023
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

package com.faendir.zachtronics.bot.om.repository

import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScoreManifold
import com.faendir.zachtronics.bot.repository.CategoryRecord
import com.faendir.zachtronics.bot.utils.newEnumSet
import java.nio.file.Path

internal data class OmMemoryRecord(
    val record: OmRecord,
    val solutionPath: Path,
    val frontierManifolds: MutableSet<OmScoreManifold> = newEnumSet<OmScoreManifold>(),
    val categories: MutableSet<OmCategory> = newEnumSet<OmCategory>()
) {
    fun toCategoryRecord() = CategoryRecord(record, categories)
}

internal fun OmRecord.toMemoryRecord(repoPath: Path) = OmMemoryRecord(this, repoPath.resolve(this.dataPath))

