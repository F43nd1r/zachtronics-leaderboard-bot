package com.faendir.om.discord.commands

import com.faendir.om.discord.leaderboards.Leaderboard
import com.faendir.om.discord.leaderboards.UpdateResult
import com.faendir.om.discord.model.Score
import com.faendir.om.discord.puzzle.Puzzle
import com.faendir.om.discord.utils.find
import com.faendir.om.discord.utils.reply
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.springframework.stereotype.Component

@Component
class SubmitCommand(private val leaderboards: List<Leaderboard>) : Command {
    override val regex =
        Regex("!submit\\s+(?<puzzle>[^:]*)(:|\\s)\\s*(?<score>[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?(/[\\d.]+[a-zA-Z]?)?)(\\s+(?<link>http.*\\.(gif|gifv|mp4|webm)))?\\s*")
    override val name: String = "submit"
    override val helpText: String =
        "!submit <puzzle>:<score1/score2/score3>(e.g. 3.5w/320c/400g) <link>(or attach file to message)"
    override val requiresRoles: List<String> = listOf("trusted-leaderboard-poster")

    override fun handleMessage(author: User, channel: TextChannel, message: Message, command: MatchResult) {
        val puzzleName = command.groups["puzzle"]!!.value
        val puzzles = Puzzle.findByName(puzzleName)
        if (puzzles.size > 1) {
            channel.reply(
                author, "sorry, your request for \"$puzzleName\" was not accurate enough. " +
                        if (puzzles.size <= 5) "Use one of:\n${
                            puzzles.joinToString("\n") { it.displayName }
                        }" else "${puzzles.size} matches."
            )
            return
        }
        if (puzzles.isEmpty()) {
            channel.reply(author, "sorry, I did not recognize the puzzle \"$puzzleName\".")
            return
        }
        val puzzle = puzzles.first()
        val scoreString = command.groups["score"]!!.value
        val score: Score = Score.parse(puzzle, scoreString) ?: return Unit.apply {
            channel.reply(author, "sorry, I couldn't parse your score ($scoreString).")
        }
        val leaderboardCategories = leaderboards.find(puzzle, score)
        if (leaderboardCategories.isEmpty()) {
            channel.reply(
                author,
                "sorry, I could not find any category for ${score.parts.keys.joinToString("/") { it.key.toString() }}"
            )
            return
        }
        val link = command.groups["link"]?.value ?: message.attachments.firstOrNull()
            ?.takeIf { listOf("gif", "gifv", "mp4", "webm").contains(it.fileExtension) }?.url ?: return Unit.also {
            channel.reply(author, "sorry, I could not find a valid link or attachment in your message.")
        }
        val results = leaderboardCategories.map { (leaderboard, categories) ->
            leaderboard.update(author.name, puzzle, categories, score, link)
        }
        val successes = results.filterIsInstance<UpdateResult.Success>()
        val pareto = results.filterIsInstance<UpdateResult.ParetoUpdate>()
        val betterExists = results.filterIsInstance<UpdateResult.BetterExists>()
        val brokenLink = results.filterIsInstance<UpdateResult.BrokenLink>()
        channel.reply(
            author, when {
                successes.isNotEmpty() -> "thanks, the site will be updated shortly with ${puzzle.displayName} ${
                    successes.flatMap { it.oldScores.keys }.map { it.displayName }
                } ${
                    score.reorderToStandard().toString("/")
                } (previously ${
                    successes.flatMap { it.oldScores.entries }
                        .joinToString {
                            "`${it.key.displayName} ${
                                it.value?.reorderToStandard()?.toString("/") ?: "none"
                            }`"
                        }
                })."
                pareto.isNotEmpty() -> "thanks, your submission for ${puzzle.displayName} (${
                    score.reorderToStandard().toString("/")
                }) was included in the pareto frontier."
                betterExists.isNotEmpty() -> "sorry, your submission did not beat any of the existing scores for ${puzzle.displayName} ${
                    betterExists.flatMap { it.scores.entries }
                        .joinToString { "`${it.key.displayName} ${it.value.reorderToStandard().toString("/")}`" }
                }"
                brokenLink.isNotEmpty() -> "sorry, I could not load the file at $link."
                else -> "sorry, something went wrong."
            }
        )
    }
}