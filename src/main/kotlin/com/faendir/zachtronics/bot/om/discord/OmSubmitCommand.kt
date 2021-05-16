package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.zachtronics.bot.generic.discord.AbstractSubmitCommand
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.om.model.OmModifier
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OpusMagnum
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.entity.User
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

@Component
class OmSubmitCommand(private val opusMagnum: OpusMagnum, override val leaderboards: List<Leaderboard<*, *, OmPuzzle, OmRecord>>) :
    AbstractSubmitCommand<OmPuzzle, OmRecord>() {

    val regex =
        Regex("!submit\\s+(?<puzzle>[^:]*)(:|\\s)\\s*(?<score>([a-zA-Z]:)?[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?(/[\\d.]+[a-zA-Z]?)?)(\\s+(?<link>http.*))?\\s*")
    val withArchiveRegex = Regex("!submit\\s+(?<score>\\S+)(\\s+(?<link1>\\S+))?(\\s+(?<link2>\\S+))?")

    override fun buildData() = SubmitParser.buildData()

    override fun parseSubmission(options: List<ApplicationCommandInteractionOption>, user: User): Mono<Tuple2<OmPuzzle, OmRecord>> {
        return Mono.fromCallable { SubmitParser.parse(options) }
            .map {
                val puzzle = opusMagnum.parsePuzzle(it.puzzle)
                Tuples.of(puzzle, OmRecord(opusMagnum.parseScore(puzzle, it.score).copy(modifier = it.modifier), it.link, user.username))
            }
    }
}

@ApplicationCommand(subCommand = true)
data class Submit(val puzzle: String, val score: String, val link: String, val modifier: OmModifier?)