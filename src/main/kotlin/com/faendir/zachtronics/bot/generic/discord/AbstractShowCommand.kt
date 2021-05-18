package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.*
import com.faendir.zachtronics.bot.utils.throwIfEmpty
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.entity.User
import discord4j.discordjson.json.WebhookExecuteRequest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.util.function.Tuple2

abstract class AbstractShowCommand<C : Category, P : Puzzle, R : Record> : Command {
    abstract val leaderboards: List<Leaderboard<C, P, R>>
    override val name: String = "show"
    override val isReadOnly: Boolean = true

    override fun handle(options: List<ApplicationCommandInteractionOption>, user: User): Mono<WebhookExecuteRequest> {
        return findPuzzleAndCategory(options).flatMap { (puzzle, category) ->
            leaderboards.toFlux()
                .flatMap { it.get(puzzle, category) }
                .next()
                .throwIfEmpty { "sorry, there is no score for ${puzzle.displayName} ${category.displayName}." }
                .map {
                    WebhookExecuteRequest.builder()
                        .content("*${puzzle.displayName}* **${category.displayName}**\n${it.toDisplayString()}")
                        .build()
                }
        }
    }

    abstract fun findPuzzleAndCategory(options: List<ApplicationCommandInteractionOption>): Mono<Tuple2<P, C>>
}