package com.faendir.zachtronics.bot.model.om

import com.faendir.zachtronics.bot.discord.commands.match
import com.faendir.zachtronics.bot.leaderboards.git.GitLeaderboard
import com.faendir.zachtronics.bot.leaderboards.reddit.RedditLeaderboard
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.utils.Result
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class OpusMagnum(gitLeaderboard: GitLeaderboard, redditLeaderboard: RedditLeaderboard) : Game<OmCategory, OmScore, OmPuzzle, OmRecord> {
    override val discordChannel = "opus-magnum"
    override fun parsePuzzle(name: String) = OmPuzzle.values().filter { it.displayName.contains(name, ignoreCase = true) }
    override fun parseScore(puzzle: OmPuzzle, string: String): OmScore? {
        if (string.isBlank()) return null
        val parts = string.split('/')
        if (parts.size < 3) return null
        if (string.contains(Regex("[a-zA-Z]"))) {
            return OmScore((parts.map { OmScorePart.parse(it) ?: return null }).toMap(LinkedHashMap()))
        }
        if (parts.size == 4) {
            return OmScore(linkedMapOf(OmScorePart.COST to parts[0].toDouble(),
                OmScorePart.CYCLES to parts[1].toDouble(),
                OmScorePart.AREA to parts[2].toDouble(),
                OmScorePart.INSTRUCTIONS to parts[3].toDouble()))
        }
        if (parts.size == 3) {
            return OmScore(linkedMapOf(OmScorePart.COST to parts[0].toDouble(),
                OmScorePart.CYCLES to parts[1].toDouble(),
                (if (puzzle.type == OmType.PRODUCTION) OmScorePart.INSTRUCTIONS else OmScorePart.AREA) to parts[2].toDouble()))
        }
        return null
    }

    override val leaderboards = listOf(gitLeaderboard, redditLeaderboard)
    override val submissionSyntax: String = "<puzzle>:<score1/score2/score3>(e.g. 3.5w/320c/400g) <link>(or attach file to message)"
    val regex = Regex("!submit\\s+(?<puzzle>[^:]*)(:|\\s)\\s*(?<score>[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?(/[\\d.]+[a-zA-Z]?)?)(\\s+(?<link>http.*\\.(gif|gifv|mp4|webm)))?\\s*")
    override fun parseSubmission(message: Message): Result<Pair<List<OmPuzzle>, OmRecord>> {
        return message.match(regex).flatMap { command ->
            val puzzles = parsePuzzle(command.groups["puzzle"]!!.value)
            val scoreString = command.groups["score"]!!.value
            val score = parseScore(puzzles.first(), scoreString) ?: return@flatMap Result.Failure("sorry, I couldn't parse your score ($scoreString).")
            val link = command.groups["link"]?.value ?: message.attachments.firstOrNull()?.takeIf { listOf("gif", "gifv", "mp4", "webm").contains(it.fileExtension) }?.url
            ?: return@flatMap Result.Failure("sorry, I could not find a valid link or attachment in your message.")
            Result.Success(puzzles to OmRecord(score, link, message.author.name))
        }

    }

    override fun parseCategory(name: String): List<OmCategory> = OmCategory.values().filter { it.displayName.equals(name, ignoreCase = true) }
}