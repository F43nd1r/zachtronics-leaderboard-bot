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

package com.faendir.zachtronics.bot.om.repository

import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import java.io.File

abstract class AbstractOmPageGenerator(private val directoryCategories: Map<String, List<OmCategory>>, ) {

    fun update(readWriteAccess: GitRepository.ReadWriteAccess, categories: List<OmCategory>, data: Map<OmPuzzle, Map<OmRecord, Set<OmCategory>>>) {
        directoryCategories.forEach { (dirName, directoryCategories) ->
            val updateCategories = categories.filter { directoryCategories.contains(it) }
            if(updateCategories.isNotEmpty()) {
                readWriteAccess.updatePage(File(readWriteAccess.repo, dirName), updateCategories, data)
            }
        }
    }

    protected abstract fun GitRepository.ReadWriteAccess.updatePage(dir: File, categories: List<OmCategory>, data: Map<OmPuzzle, Map<OmRecord, Set<OmCategory>>>)
}