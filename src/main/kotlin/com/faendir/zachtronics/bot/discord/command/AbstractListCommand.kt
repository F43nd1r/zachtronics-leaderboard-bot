package com.faendir.zachtronics.bot.discord.command

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.utils.asMultipartRequest
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
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
                .embedCategoryRecords(records.entries)
                .apply {
                    val missing = categories.minus(records.keys)
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