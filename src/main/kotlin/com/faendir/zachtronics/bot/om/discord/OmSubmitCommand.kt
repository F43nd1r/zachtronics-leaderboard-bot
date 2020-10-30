package com.faendir.zachtronics.bot.om.discord

import com.faendir.zachtronics.bot.generic.discord.AbstractSubmitCommand
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.om.archive.OmArchive
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OpusMagnum
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.and
import com.faendir.zachtronics.bot.utils.match
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class OmSubmitCommand(private val opusMagnum: OpusMagnum, override val leaderboards: List<Leaderboard<*, *, OmPuzzle, OmRecord>>,
                      private val archiveCommand: OmArchiveCommand, private val archive: OmArchive) : AbstractSubmitCommand<OmPuzzle, OmRecord>() {
    override val helpText: String = "<puzzle>:<score1/score2/score3>(e.g. 3.5w/320c/400g) <link>(or attach file to message)"

    val regex = Regex("!submit\\s+(?<puzzle>[^:]*)(:|\\s)\\s*(?<score>[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?(/[\\d.]+[a-zA-Z]?)?)(\\s+(?<link>http.*))?\\s*")
    val withArchiveRegex = Regex("!submit\\s+(?<score>\\S+)(\\s+(?<link1>\\S+))?(\\s+(?<link2>\\S+))?")

    override fun parseSubmission(message: Message): Result<Pair<OmPuzzle, OmRecord>> {
        return message.match(regex).flatMap { command ->
            opusMagnum.parsePuzzle(command.groups["puzzle"]!!.value)
                .and { opusMagnum.parseScore(it, command.groups["score"]!!.value) }
                .and { _, _ -> findLink(command, message) }
                .map { (puzzle, score, link) -> puzzle to OmRecord(score, link, message.author.name) }
        }.onFailureTry {
            message.match(withArchiveRegex).flatMap { command ->
                archiveCommand.findScorePart(command, "score").flatMap { scorePart ->
                    val links = findAllLinks(command, message)
                    links.findSolutionLink().and { links.findGifLink() }.flatMap { (solutionLink, gifLink) ->
                        archiveCommand.parseSolution(scorePart, solutionLink).map { solution ->
                            archive.archive(solution)
                            solution.puzzle to OmRecord(solution.score, gifLink, message.author.name)
                        }
                    }
                }
            }
        }
    }

    private fun List<String>.findSolutionLink(): Result<String> {
        return find { it.endsWith(".solution") }?.let { Result.success(it) } ?: Result.failure("could not find solution")
    }

    private fun List<String>.findGifLink(): Result<String> {
        return find { !it.endsWith(".solution") }?.let { Result.success(it) } ?: Result.failure("could not find solution")
    }

    private fun findAllLinks(command: MatchResult, message: Message): List<String> {
        return listOfNotNull(command.groups["link1"]?.value, command.groups["link2"]?.value) + message.attachments.map { it.url }
    }

    private fun findLink(command: MatchResult, message: Message): Result<String> {
        return (command.groups["link"]?.value ?: message.attachments.firstOrNull()?.url)?.let { Result.success(it) }
            ?: Result.parseFailure("I could not find a valid link or attachment in your message.")
    }
}