package com.faendir.zachtronics.bot.om.leaderboards

import com.faendir.zachtronics.bot.main.git.GitRepository
import com.faendir.zachtronics.bot.om.imgur.ImgurService
import com.faendir.zachtronics.bot.om.model.*
import com.faendir.zachtronics.bot.om.model.OmCategory.*
import kotlinx.serialization.serializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class OmGithubPagesLeaderboard(@Qualifier("omGithubPagesLeaderboardRepository") gitRepository: GitRepository, imgurService: ImgurService) :
    AbstractOmJsonLeaderboard<MutableMap<OmPuzzle, MutableMap<OmCategory, OmRecord>>>(
        gitRepository,
        imgurService,
        mapOf(
            "wh" to listOf(HEIGHT, WIDTH),
            "og" to listOf(OGC, OGA, OGX),
            "oc" to listOf(OCG, OCA, OCX),
            "oa" to listOf(OAG, OAC, OAX),
            "s4" to listOf(S4G, S4C, S4A, S4I),
            "ti" to listOf(TIG, TIC, TIA),
            "i" to listOf(IGNP, ICNP, IANP, CINP)
        ),
        serializer()
    ) {

    private val tableHeaders = mapOf(
        HEIGHT to "Height",
        WIDTH to "Width",
        OGC to "Cost/Cycles",
        OGA to "Cost/Area",
        OGX to "Cost/Cycles*Area",
        OCG to "Cycles/Cost",
        OCA to "Cycles/Area",
        OCX to "Cycles/Cost*Area",
        OAG to "Area/Cost",
        OAC to "Area/Cycles",
        OAX to "Area/Cost*Cycles",
        S4G to "Sum 4/Cost",
        S4C to "Sum 4/Cycles",
        S4A to "Sum 4/Area",
        S4I to "Sum 4/Instructions",
        TIG to "Instructions/Cost",
        TIC to "Instructions/Cycles",
        TIA to "Instructions/Area",
        IGNP to "Instructions/Cost",
        ICNP to "Instructions/Cycles",
        IANP to "Instructions/Area",
        CINP to "Cycles/Instructions",
    )

    override fun MutableMap<OmPuzzle, MutableMap<OmCategory, OmRecord>>.getRecord(puzzle: OmPuzzle, category: OmCategory) =
        get(puzzle)?.get(category)?.apply { score.updateTransient(category) }

    override fun MutableMap<OmPuzzle, MutableMap<OmCategory, OmRecord>>.setRecord(puzzle: OmPuzzle, category: OmCategory, record: OmRecord) {
        set(puzzle, (get(puzzle) ?: mutableMapOf()).apply { set(category, record) })
    }

    override fun GitRepository.AccessScope.updatePage(
        dir: File, categories: List<OmCategory>,
        records: MutableMap<OmPuzzle, MutableMap<OmCategory, OmRecord>>
    ) {
        val templates = File(repo, "templates")
        val mainTemplate = File(templates, "main.html").readText()
        val groupTemplate = File(templates, "group.html").readText()
        val puzzleTemplate = File(templates, "puzzle.html").readText()
        val blockTemplate = File(templates, "block.html").readText()
        val scoreTemplate = File(templates, "score.html").readText()
        val gifTemplate = File(templates, "gif.html").readText()
        val videoTemplate = File(templates, "video.html").readText()
        val headerTemplate = File(templates, "columnheader.html").readText()
        val cellTemplate = File(templates, "cell.html").readText()
        val explanation = File(dir, "explanation.html").readText()

        val text = mainTemplate.format(explanation, OffsetDateTime.now(ZoneOffset.UTC), OmGroup.values().joinToString("\n") { group ->
            val puzzles = OmPuzzle.values().filter { it.group == group && it.type != OmType.PRODUCTION }
            if (puzzles.isNotEmpty()) {
                groupTemplate.format(group.displayName, categories.joinToString("\n") { category ->
                    headerTemplate.format(tableHeaders[category])
                }, puzzles.joinToString("\n") { puzzle ->
                    puzzleTemplate.format(puzzle.displayName, categories.joinToString("\n") { category ->
                        cellTemplate.format(if (category.supportsPuzzle(puzzle)) {
                            records[puzzle]?.get(category)?.let {
                                scoreTemplate.format(it.link, if (category == HEIGHT || category == WIDTH) {
                                    it.score.toDisplayString({ category.sortScoreParts(this) }) { _, value -> format(value) }
                                } else {
                                    it.score.toShortDisplayString()
                                }, if (it.link.endsWith("mp4") || it.link.endsWith("webm")) {
                                    videoTemplate.format(it.link)
                                } else {
                                    gifTemplate.format(it.link)
                                })
                            } ?: ""
                        } else {
                            blockTemplate
                        })
                    })
                })
            } else {
                ""
            }
        })
        val file = File(dir, "index.html")
        val old = file.takeIf { it.exists() }?.readText() ?: ""
        if (old.lines().filter { !it.contains("last updated on") } != text.lines().filter { !it.contains("last updated on") }) {
            file.writeText(text)
            add(file)
        }
    }
}