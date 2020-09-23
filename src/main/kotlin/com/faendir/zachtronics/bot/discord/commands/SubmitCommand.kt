package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.model.*
import com.faendir.zachtronics.bot.utils.findInstance
import com.faendir.zachtronics.bot.utils.ifNotEmpty
import com.faendir.zachtronics.bot.utils.message
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class SubmitCommand : Command {
    override val name: String = "submit"
    override fun helpText(game: Game<*, *, *, *>): String = "!submit ${game.submissionSyntax}"
    override val requiresRoles: List<String> = listOf("trusted-leaderboard-poster")

    override fun <C : Category<C, S, P>, S : Score, P : Puzzle, R : Record<S>> handleMessage(game: Game<C, S, P, R>, message: Message): String {
        return game.parseSubmission(message).flatMap { (puzzles, record) -> puzzles.getSinglePuzzle().map { it to record } }.map { (puzzle, record) ->
            val results = game.leaderboards.map { it.update(puzzle, record) }
            results.filterIsInstance<UpdateResult.Success<C, S>>().ifNotEmpty { successes ->
                return@map "thanks, the site will be updated shortly with ${puzzle.displayName} ${
                    successes.flatMap { it.oldScores.keys }.map { it.displayName }
                } ${record.score.toDisplayString()} (previously ${
                    successes.flatMap { it.oldScores.entries }.joinToString { "`${it.key.displayName} ${it.value?.toDisplayString() ?: "none"}`" }
                })."
            }
            results.findInstance<UpdateResult.ParetoUpdate<C, S>> {
                return@map "thanks, your submission for ${puzzle.displayName} (${record.score.toDisplayString()}) was included in the pareto frontier."
            }
            results.filterIsInstance<UpdateResult.BetterExists<C, S>>().ifNotEmpty { betterExists ->
                return@map "sorry, your submission did not beat any of the existing scores for ${puzzle.displayName} ${
                    betterExists.flatMap { it.scores.entries }.joinToString { "`${it.key.displayName} ${it.value.toDisplayString()}`" }
                }"
            }
            results.findInstance<UpdateResult.NotSupported<C, S>> { return@map "sorry, no leaderboard supporting your submission found" }
            "sorry, something went wrong"
        }.message
    }
}