package com.faendir.zachtronics.bot.model.om

import com.faendir.zachtronics.bot.leaderboards.om.OmLeaderboard
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.Result.Companion.parseFailure
import com.faendir.zachtronics.bot.utils.Result.Companion.success
import com.faendir.zachtronics.bot.utils.and
import com.faendir.zachtronics.bot.utils.getSingleMatchingPuzzle
import com.faendir.zachtronics.bot.utils.match
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class OpusMagnum(override val leaderboards: List<OmLeaderboard>) : Game<OmCategory, OmScore, OmPuzzle, OmRecord> {

    override val discordChannel = "opus-magnum"

    override fun parsePuzzle(name: String): Result<OmPuzzle> = OmPuzzle.values().getSingleMatchingPuzzle(name)

    internal fun parseScore(puzzle: OmPuzzle, string: String): Result<OmScore> {
        if (string.isBlank()) return parseFailure("I didn't find a score in your command.")
        val parts = string.split('/')
        if (parts.size < 3) return parseFailure("your score must have at least three parts.")
        if (string.contains(Regex("[a-zA-Z]"))) {
            return success(OmScore((parts.map { OmScorePart.parse(it) ?: return parseFailure("I didn't understand \"$it\".") })))
        }
        if (parts.size == 4) {
            return success(OmScore(OmScorePart.COST to parts[0].toDouble(),
                OmScorePart.CYCLES to parts[1].toDouble(),
                OmScorePart.AREA to parts[2].toDouble(),
                OmScorePart.INSTRUCTIONS to parts[3].toDouble()))
        }
        if (parts.size == 3) {
            return success(OmScore(OmScorePart.COST to parts[0].toDouble(),
                OmScorePart.CYCLES to parts[1].toDouble(),
                (if (puzzle.type == OmType.PRODUCTION) OmScorePart.INSTRUCTIONS else OmScorePart.AREA) to parts[2].toDouble()))
        }
        return parseFailure("you need to specify score part identifiers when using more than four values.")
    }

    private fun findLink(command: MatchResult, message: Message): Result<String> {
        return (command.groups["link"]?.value ?: message.attachments.firstOrNull()?.url)?.let { success(it) }
            ?: parseFailure("I could not find a valid link or attachment in your message.")
    }

    override val submissionSyntax: String = "<puzzle>:<score1/score2/score3>(e.g. 3.5w/320c/400g) <link>(or attach file to message)"
    val regex = Regex("!submit\\s+(?<puzzle>[^:]*)(:|\\s)\\s*(?<score>[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?(/[\\d.]+[a-zA-Z]?)?)(\\s+(?<link>http.*))?\\s*")

    override fun parseSubmission(message: Message): Result<Pair<OmPuzzle, OmRecord>> {
        return message.match(regex).flatMap { command ->
            parsePuzzle(command.groups["puzzle"]!!.value).and { parseScore(it, command.groups["score"]!!.value) }
                .and { _, _ -> findLink(command, message) }
                .map { (puzzle, score, link) -> puzzle to OmRecord(score, link, message.author.name) }
        }
    }

    override fun parseCategory(name: String): List<OmCategory> = OmCategory.values().filter { it.displayName.equals(name, ignoreCase = true) }

    override fun hasWritePermission(member: Member?): Boolean = member?.roles?.any { it.name == "trusted-leaderboard-poster" } ?: false
}