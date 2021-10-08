/*
 * Copyright (c) 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.utils

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.discordjson.json.EmbedFieldData
import discord4j.discordjson.json.ImmutableEmbedData
import discord4j.discordjson.json.MessageSendRequestBase
import discord4j.rest.util.MultipartRequest

inline fun <T, R> Collection<T>.ifNotEmpty(block: (Collection<T>) -> R): R? = takeIf { it.isNotEmpty() }?.let(block)

inline fun <reified T> Iterable<*>.findInstance(): T? = filterIsInstance<T>().firstOrNull()

inline fun <reified T> Iterable<*>.findInstance(block: (T) -> Unit) = findInstance<T>()?.let(block)

fun <T : Map.Entry<K, V>, K, V> Iterable<T>.toMap() = associate { it.key to it.value }

fun <T> Iterable<T>.plusIf(condition: Boolean, element: T): List<T> = if (condition) plus(element) else toList()

private val wordSeparator = Regex("[\\s-/,:]+")

fun <P : Puzzle> Array<P>.getSingleMatchingPuzzle(name: String): P {
    val matches = toList().fuzzyMatch(name.trim()) { displayName }
    return when (val size = matches.size) {
        0 -> throw IllegalArgumentException("I did not recognize the puzzle \"$name\".")
        1 -> matches.first()
        in 2..5 -> throw IllegalArgumentException("your request for \"$name\" was not precise enough. Use one of:\n${matches.joinToString("\n") { "- *${it.displayName}*" }}")
        else -> throw IllegalArgumentException("your request for \"$name\" was not precise enough. $size matches.")
    }
}

fun <P> Collection<P>.fuzzyMatch(search: String, name: P.() -> String): List<P> {
    return search.takeIf { it.isEmpty() }?.let { emptyList() }
        ?: find {
            // exact match
            it.name().equals(search, ignoreCase = true)
        }?.let { listOf(it) }
        ?: filter {
            //abbreviation
            val words = it.name().split(wordSeparator)
            search.length == words.size && words.zip(search.asIterable()).map { (word, char) -> word.startsWith(char, ignoreCase = true) }.reduce(Boolean::and)
        }.takeIf { it.isNotEmpty() }
        ?: filter {
            //words contain match
            val words = it.name().split(wordSeparator).iterator()
            for (part in search.split(wordSeparator)) {
                do {
                    if (!words.hasNext()) return@filter false
                } while (!words.next().contains(part, ignoreCase = true))
            }
            true
        }
}

fun <T : MessageSendRequestBase> T.asMultipartRequest() = MultipartRequest.ofRequest(this)

fun <C : Category, R : Record> ImmutableEmbedData.Builder.embedCategoryRecords(records: Iterable<Map.Entry<C, R?>>):
        ImmutableEmbedData.Builder {
    return this.addAllFields(
        records.groupBy({ it.value?.toEmbedDisplayString() ?: "`none`" }, { it.key })
            .map { (record, categories) -> record to categories.sortedBy<C, Comparable<*>> { it as Comparable<*> } }
            .sortedBy<Pair<String, List<C>>, Comparable<*>> { it.second.first() as Comparable<*> }
            .map { (record, categories) ->
                EmbedFieldData.builder()
                    .name(categories.joinToString("/") { it.displayName })
                    .value(record)
                    .inline(true)
                    .build()
            }
    )
}

fun ChatInputInteractionEvent.user(): User =
    this.interaction.member.map { it as User }.orElse(this.interaction.user)