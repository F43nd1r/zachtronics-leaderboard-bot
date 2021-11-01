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

package com.faendir.zachtronics.bot.om.archive

import com.faendir.zachtronics.bot.archive.Archive
import com.faendir.zachtronics.bot.archive.ArchiveResult
import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmSolution
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.File

@Component
class OmArchive(@Qualifier("omArchiveRepository") private val gitRepo: GitRepository) :
    Archive<OmSolution> {

    /*@PostConstruct
    fun importOldStuff() {
        val normalDir = ResourceUtils.getFile("classpath:width")
        normalDir.listFiles()?.forEach { file ->
            val solution = SolutionParser.parse(file.inputStream().asInput()) as SolvedSolution
            archive(OmSolution(OmPuzzle.values().first { it.id == solution.puzzle },
                OmScore(OmScorePart.WIDTH to file.name.split('_')[2].removePrefix("W").toDouble(),
                    OmScorePart.COST to solution.cost.toDouble(),
                    OmScorePart.CYCLES to solution.cycles.toDouble(),
                    OmScorePart.AREA to solution.area.toDouble(),
                    OmScorePart.INSTRUCTIONS to solution.instructions.toDouble()),
                DslGenerator.toDsl(solution)))
        }
    }*/

    override fun archive(solution: OmSolution): ArchiveResult = gitRepo.access {
        val dir = getPuzzleDir(solution.puzzle)
        val changed = OmCategory.values().filter { it.supportsPuzzle(solution.puzzle) && it.supportsScore(solution.score) }.filter { category ->
            val oldFile = dir.list()?.find { it.startsWith(category.displayName) }
            val oldScore = oldFile?.split('_')
                ?.getOrNull(1)
                ?.let { scoreString -> OmScore.parse(solution.puzzle, scoreString) }
            val file = File(dir, "${category.displayName}_${solution.score.toFileString(category)}_${solution.puzzle.name}.solution.kts")
            when {
                oldScore == null -> {
                    dir.mkdirs()
                    file.writeText(solution.solution)
                    add(file)
                    true
                }
                category.scoreComparator.compare(solution.score, oldScore) <= 0 -> {
                    dir.mkdirs()
                    file.writeText(solution.solution)
                    add(file)
                    val old = File(dir, oldFile)
                    if(old != file) {
                        rm(old)
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }
        if (changed.any()) {
            if (status().isClean) {
                ArchiveResult.AlreadyArchived()
            } else {
                commitAndPush("${solution.puzzle.displayName} ${solution.score.toDisplayString()} $changed")
                ArchiveResult.Success("**${changed.joinToString { it.displayName }}**")
            }
        } else {
            ArchiveResult.Failure()
        }
    }

    fun getAll(puzzle: OmPuzzle): List<OmSolution> = gitRepo.access {
        getPuzzleDir(puzzle).listFiles()?.map {
            val score = OmScore.parse(puzzle, it.name.split("_")[1])
            OmSolution(puzzle, score, it.readText())
        } ?: emptyList()
    }

    private fun GitRepository.AccessScope.getPuzzleDir(puzzle: OmPuzzle): File = File(repo, "${puzzle.group.name}/${puzzle.name}")

    private fun OmScore.toFileString(category: OmCategory) =
        toDisplayString({ category.sortScoreParts(this) }, "-", { part, value -> "${format(value)}${part.key}" })
}