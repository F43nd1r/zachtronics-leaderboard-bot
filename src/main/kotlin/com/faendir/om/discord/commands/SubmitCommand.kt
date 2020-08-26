package com.faendir.om.discord.commands

import com.faendir.om.discord.categories.Category
import com.faendir.om.discord.categories.ScorePart
import com.faendir.om.discord.leaderboards.Leaderboard
import com.faendir.om.discord.leaderboards.PuzzleResult
import com.faendir.om.discord.leaderboards.UpdateResult
import com.faendir.om.discord.utils.Score
import com.faendir.om.discord.utils.find
import com.faendir.om.discord.utils.reply
import com.faendir.om.discord.utils.toScoreString
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.springframework.stereotype.Component

@Component
class SubmitCommand(private val leaderboards: List<Leaderboard<*>>) : Command {
    override val regex =
        Regex("!submit\\s+(?<puzzle>[^:]*)(:|\\s)\\s*(?<score>([\\d.]+\\w)(/[\\d.]+\\w)*)(\\s+(?<link>http.*\\.(gif|mp4|webm)))?")
    override val name: String = "submit"
    override val helpText: String =
        "!submit <puzzle>:<score1/score2/score3>(e.g. 3.5w/320c/400g) <link>(or attach file to message)"

    override fun handleMessage(author: User, channel: TextChannel, message: Message, command: MatchResult) {
        if(author.flags)
        val score: Score = command.groups["score"]!!.value.split('/').map {
            ScorePart.parse(it) ?: return Unit.apply {
                channel.reply(author, "sorry, I could parse part of your score ($it).")
            }
        }
        val leaderboardCategories = leaderboards.find(score)
        if (leaderboardCategories.isEmpty()) {
            channel.reply(
                author,
                "sorry, I could not find any category for ${score.joinToString("/") { it.first.key.toString() }}"
            )
            return
        }
        val puzzle = command.groups["puzzle"]!!.value
        val link = command.groups["link"]?.value ?: message.attachments.firstOrNull()
            ?.takeIf { listOf("gif", "mp4", "webm").contains(it.fileExtension) }?.url ?: return Unit.also {
            channel.reply(author, "sorry, I could not find a valid link or attachment in your message.")
        }
        val puzzles = leaderboardCategories.map { (leaderboard, category) ->
            LeaderboardCategoryPuzzle.create(leaderboard, category, puzzle)
        }
        val ambiguous = puzzles.map { it.result }.filterIsInstance<PuzzleResult.Ambiguous<*>>()
        if (ambiguous.isNotEmpty()) {
            val options = ambiguous.flatMap { it.puzzles.asIterable() }.distinct()
            channel.reply(
                author, "sorry, your request for \"$puzzle\" was not accurate enough. " +
                        if (options.size <= 5) "Use one of:\n${
                            options.joinToString("\n")
                        }" else "$options.size} matches."
            )
            return
        }
        val successes = puzzles.filter { it.result is PuzzleResult.Success<*> }
        if (successes.isEmpty()) {
            channel.reply(author, "sorry, I did not recognize the puzzle \"$puzzle\".")
            return
        }
        fun <T> getResponses(leaderboardCategoryPuzzle: LeaderboardCategoryPuzzle<T>): String {
            val leaderboard = leaderboardCategoryPuzzle.leaderboard
            val category = leaderboardCategoryPuzzle.category
            val success = leaderboardCategoryPuzzle.result as PuzzleResult.Success<T>
            return "${category.name}: " + when (val updateResult =
                leaderboard.update(author.name, success.puzzle, category, category.normalizeScore(score), link)) {
                is UpdateResult.Success -> "thanks, the site will be updated shortly with ${updateResult.puzzle} ${category.name} ${
                    score.toScoreString("/")
                } " + (updateResult.oldScore?.let { "(previously ${category.name} ${it.toScoreString("/")})." }
                    ?: ".")
                is UpdateResult.BetterExists -> "sorry, there is already a better score for ${updateResult.puzzle}: ${category.name} ${
                    updateResult.score.toScoreString("/")
                }."
                UpdateResult.BrokenLink -> "sorry, I could not load the file at $link."
                is UpdateResult.GenericFailure -> {
                    updateResult.exception.printStackTrace()
                    "sorry, something went wrong."
                }
            }
        }

        val responses = successes.map { getResponses(it) }
        channel.reply(
            author,
            "your submission matched ${responses.size} ${if (responses.size == 1) "category" else "categories"}:\n" +
                    responses.joinToString("\n")
        )
    }


    data class LeaderboardCategoryPuzzle<T>(
        val leaderboard: Leaderboard<T>,
        val category: Category,
        val result: PuzzleResult<T>
    ) {
        companion object {
            fun <T> create(leaderboard: Leaderboard<T>, category: Category, puzzle: String) =
                LeaderboardCategoryPuzzle(leaderboard, category, leaderboard.findPuzzle(puzzle))

        }
    }
}