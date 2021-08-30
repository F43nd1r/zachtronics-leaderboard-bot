package com.faendir.zachtronics.bot.discord.command

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.utils.asMultipartRequest
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.EmbedData
import discord4j.discordjson.json.EmbedFieldData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest

abstract class AbstractListCommand<C : Category, P : Puzzle, R : Record> : AbstractCommand() {
    abstract val leaderboards: List<Leaderboard<C, P, R>>

    override fun handle(event: SlashCommandEvent): MultipartRequest<WebhookExecuteRequest> {
        val (puzzle, categories) = findPuzzleAndCategories(event)
        val records = leaderboards.map { it.getAll(puzzle, categories) }.reduce { acc, map -> acc + map }
        return WebhookExecuteRequest.builder()
            .addEmbed(EmbedData.builder()
                .title("*${puzzle.displayName}*")
                .addAllFields(
                    records.asIterable()
                        .groupBy({ it.value }, { it.key })
                        .map { entry -> entry.key to entry.value.sortedBy<C, Comparable<*>> { it as? Comparable<*> } }
                        .sortedBy<Pair<R, List<C>>, Comparable<*>> { it.second.first() as? Comparable<*> }
                        .map { (record, categories) ->
                            EmbedFieldData.builder()
                                .name(categories.joinToString("/") { it.displayName })
                                .value(record.let { "[${it.score.toDisplayString()}](${it.link})" })
                                .inline(true)
                                .build()
                        }
                )
                .apply {
                    val missing = categories.minus(records.map { it.key })
                    if (missing.isNotEmpty()) {
                        addField(
                            EmbedFieldData.builder()
                                .name(missing.joinToString("/") { it.displayName })
                                .value("None")
                                .inline(false)
                                .build()
                        )
                    }
                }
                .build()
            )
            .build()
            .asMultipartRequest()
    }

    /** @return pair of Puzzle and all the categories that support it */
    abstract fun findPuzzleAndCategories(interaction: SlashCommandEvent): Pair<P, List<C>>
}