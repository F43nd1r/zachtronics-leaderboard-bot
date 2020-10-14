package com.faendir.zachtronics.bot.om.discord

import com.faendir.zachtronics.bot.generic.discord.AbstractSubmitCommand
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.om.model.*
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.and
import com.faendir.zachtronics.bot.utils.match
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class OmSubmitCommand(private val opusMagnum: OpusMagnum, leaderboards: List<Leaderboard<OmCategory, OmScore, OmPuzzle, OmRecord>>) :
    AbstractSubmitCommand<OmCategory, OmScore, OmPuzzle, OmRecord>(leaderboards) {
    override val helpText: String = "<puzzle>:<score1/score2/score3>(e.g. 3.5w/320c/400g) <link>(or attach file to message)"

    val regex = Regex("!submit\\s+(?<puzzle>[^:]*)(:|\\s)\\s*(?<score>[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?(/[\\d.]+[a-zA-Z]?)?)(\\s+(?<link>http.*))?\\s*")

    override fun parseSubmission(message: Message): Result<Pair<OmPuzzle, OmRecord>> {
        return message.match(regex).flatMap { command ->
            opusMagnum.parsePuzzle(command.groups["puzzle"]!!.value)
                .and { opusMagnum.parseScore(it, command.groups["score"]!!.value) }
                .and { _, _ -> opusMagnum.findLink(command, message) }
                .map { (puzzle, score, link) -> puzzle to OmRecord(score, link, message.author.name) }
        }
    }
}