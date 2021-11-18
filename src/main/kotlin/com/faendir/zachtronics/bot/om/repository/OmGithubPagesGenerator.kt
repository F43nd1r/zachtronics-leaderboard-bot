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
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmCategory.CINP
import com.faendir.zachtronics.bot.om.model.OmCategory.HEIGHT
import com.faendir.zachtronics.bot.om.model.OmCategory.IANP
import com.faendir.zachtronics.bot.om.model.OmCategory.ICNP
import com.faendir.zachtronics.bot.om.model.OmCategory.IGNP
import com.faendir.zachtronics.bot.om.model.OmCategory.OAC
import com.faendir.zachtronics.bot.om.model.OmCategory.OAG
import com.faendir.zachtronics.bot.om.model.OmCategory.OAX
import com.faendir.zachtronics.bot.om.model.OmCategory.OCA
import com.faendir.zachtronics.bot.om.model.OmCategory.OCG
import com.faendir.zachtronics.bot.om.model.OmCategory.OCX
import com.faendir.zachtronics.bot.om.model.OmCategory.OGA
import com.faendir.zachtronics.bot.om.model.OmCategory.OGC
import com.faendir.zachtronics.bot.om.model.OmCategory.OGX
import com.faendir.zachtronics.bot.om.model.OmCategory.S4A
import com.faendir.zachtronics.bot.om.model.OmCategory.S4C
import com.faendir.zachtronics.bot.om.model.OmCategory.S4G
import com.faendir.zachtronics.bot.om.model.OmCategory.S4I
import com.faendir.zachtronics.bot.om.model.OmCategory.TIA
import com.faendir.zachtronics.bot.om.model.OmCategory.TIC
import com.faendir.zachtronics.bot.om.model.OmCategory.TIG
import com.faendir.zachtronics.bot.om.model.OmCategory.WIDTH
import com.faendir.zachtronics.bot.om.model.OmGroup
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmType
import org.springframework.stereotype.Component
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class OmGithubPagesGenerator :
    AbstractOmPageGenerator(
        mapOf(
            "wh" to listOf(HEIGHT, WIDTH),
            "og" to listOf(OGC, OGA, OGX),
            "oc" to listOf(OCG, OCA, OCX),
            "oa" to listOf(OAG, OAC, OAX),
            "s4" to listOf(S4G, S4C, S4A, S4I),
            "ti" to listOf(TIG, TIC, TIA),
            "i" to listOf(IGNP, ICNP, IANP, CINP)
        )
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

    override fun GitRepository.ReadWriteAccess.updatePage(dir: File, categories: List<OmCategory>, data: Map<OmPuzzle, Map<OmRecord, Set<OmCategory>>>) {
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
        val explanation = File(dir, "explanation.html").takeIf { it.exists() }?.readText() ?: ""

        val text = mainTemplate.format(explanation, OffsetDateTime.now(ZoneOffset.UTC), OmGroup.values().joinToString("\n") { group ->
            val puzzles = OmPuzzle.values().filter { it.group == group && it.type != OmType.PRODUCTION }
            if (puzzles.isNotEmpty()) {
                groupTemplate.format(group.displayName, categories.joinToString("\n") { category ->
                    headerTemplate.format(tableHeaders[category])
                }, puzzles.joinToString("\n") { puzzle ->
                    puzzleTemplate.format(puzzle.displayName, categories.joinToString("\n") { category ->
                        cellTemplate.format(if (category.supportsPuzzle(puzzle)) {
                            data[puzzle]?.entries?.find { it.value.contains(category) }?.key?.let {
                                scoreTemplate.format(it.displayLink, it.score.toDisplayString(DisplayContext(StringFormat.PLAIN_TEXT, category)), if (it.displayLink?.endsWith("mp4") == true || it.displayLink?.endsWith("webm") == true) {
                                    videoTemplate.format(it.displayLink)
                                } else {
                                    gifTemplate.format(it.displayLink)
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