package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.annotation.Description
import com.faendir.zachtronics.bot.generic.discord.AbstractSubmitCommand
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.om.model.OmModifier
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.utils.findLink
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3
import reactor.util.function.Tuple2

@Component
class OmSubmitCommand(override val leaderboards: List<Leaderboard<*, OmPuzzle, OmRecord>>) :
    AbstractSubmitCommand<OmPuzzle, OmRecord>() {

    override fun buildData() = SubmitParser.buildData()

    override fun parseSubmission(
        options: List<ApplicationCommandInteractionOption>,
        user: User,
        previousMessages: Flux<Message>
    ): Mono<Tuple2<OmPuzzle, OmRecord>> {
        return Mono.fromCallable { SubmitParser.parse(options) }
            .flatMap {
                Mono.zip(it.puzzle.toMono(), Mono.zip(
                    OmScore.parse(it.puzzle, it.score).copy(modifier = it.modifier).toMono(),
                    findLink(it.gif, previousMessages),
                    user.username.toMono()
                ).map { (score, link, user) -> OmRecord(score, link, user) })
            }
    }
}

@ApplicationCommand(description = "Submit a solution", subCommand = true)
data class Submit(
    @Description("Puzzle name. Can be shortened or abbreviated. E.g. `stab water`, `PMO`")
    @Converter(PuzzleConverter::class)
    val puzzle: OmPuzzle,
    @Description("Puzzle score. E.g. `100/32/14/22`, `3.5w/32c/100g`")
    val score: String,
    @Description("Link to your solution gif/mp4, can be `m1` or `m2` to scrape it from your last or second to last message respectively")
    val gif: String,
    @Description("Metric Modifier")
    val modifier: OmModifier?
)