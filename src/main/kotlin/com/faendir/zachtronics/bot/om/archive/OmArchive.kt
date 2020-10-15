package com.faendir.zachtronics.bot.om.archive

import com.faendir.zachtronics.bot.main.git.GitRepository
import com.faendir.zachtronics.bot.model.Archive
import com.faendir.zachtronics.bot.om.model.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.File

@Component
class OmArchive(@Qualifier("omArchiveRepository") private val gitRepo: GitRepository, private val opusMagnum: OpusMagnum) : Archive<OmSolution> {
    override fun archive(solution: OmSolution): List<String> {
        return gitRepo.access {
            val dir = getPuzzleDir(solution.puzzle)
            val changed = OmCategory.values().filter { it.supportsPuzzle(solution.puzzle) && it.supportsScore(solution.score) }.filter { category ->
                val oldFile = dir.list()?.find { it.startsWith(category.displayName) }
                val oldScore = oldFile?.split('_')
                    ?.getOrNull(1)
                    ?.let { scoreString -> opusMagnum.parseScore(solution.puzzle, scoreString).onFailure { throw IllegalStateException(it) } }
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
                        rm(File(dir, oldFile))
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
            if (changed.any()) {
                commitAndPush("${solution.puzzle.displayName} ${solution.score.toDisplayString()} $changed")
            }
            changed.map { it.displayName }
        }
    }

    private fun GitRepository.AccessScope.getPuzzleDir(puzzle: OmPuzzle): File = File(repo, "${puzzle.group.name}/${puzzle.name}")

    private fun OmScore.toFileString(category: OmCategory) =
        toDisplayString({ category.sortScoreParts(this) }, "-", { part, value -> "${format(value)}${part.key}" })
}