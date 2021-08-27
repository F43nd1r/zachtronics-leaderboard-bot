package com.faendir.zachtronics.bot.om.archive

import com.faendir.zachtronics.bot.main.git.GitRepository
import com.faendir.zachtronics.bot.generic.archive.Archive
import com.faendir.zachtronics.bot.om.model.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
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

    override fun archive(solution: OmSolution): Mono<Pair<String,String>> {
        return gitRepo.access {
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
                commitAndPush("${solution.puzzle.displayName} ${solution.score.toDisplayString()} $changed")
            }
            changed.joinToString { it.displayName } to ""
        }
    }

    private fun GitRepository.AccessScope.getPuzzleDir(puzzle: OmPuzzle): File = File(repo, "${puzzle.group.name}/${puzzle.name}")

    private fun OmScore.toFileString(category: OmCategory) =
        toDisplayString({ category.sortScoreParts(this) }, "-", { part, value -> "${format(value)}${part.key}" })
}