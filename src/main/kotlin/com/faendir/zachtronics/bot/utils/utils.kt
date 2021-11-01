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

import com.faendir.discord4j.command.parse.SingleParseResult
import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.repository.CategoryRecord
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.core.spec.EmbedCreateFields
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionReplyEditMono
import java.io.Closeable
import java.net.HttpURLConnection
import java.net.URL

inline fun <T, R> Collection<T>.ifNotEmpty(block: (Collection<T>) -> R): R? = takeIf { it.isNotEmpty() }?.let(block)

inline fun <reified T> Iterable<*>.findInstance(): T? = filterIsInstance<T>().firstOrNull()

inline fun <reified T> Iterable<*>.findInstance(block: (T) -> Unit) = findInstance<T>()?.let(block)

fun <T : Map.Entry<K, V>, K, V> Iterable<T>.toMap() = associate { it.key to it.value }

fun <T> Iterable<T>.plusIf(condition: Boolean, element: T): List<T> = if (condition) plus(element) else toList()

private val wordSeparator = Regex("[\\s-/,:]+")

fun <P : Puzzle> Array<P>.getSingleMatchingPuzzle(name: String): SingleParseResult<P> {
    val matches = toList().fuzzyMatch(name.trim()) { displayName }
    return when (val size = matches.size) {
        0 -> SingleParseResult.Failure("I did not recognize the puzzle \"$name\".")
        1 -> SingleParseResult.Success(matches.first())
        in 2..25 -> SingleParseResult.Ambiguous(matches) { it.displayName }
        else -> SingleParseResult.Failure("your request for \"$name\" was not precise enough. $size matches.")
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

@Suppress("UNCHECKED_CAST")
fun <R : Record<C>?, C : Category> SafeEmbedMessageBuilder.embedCategoryRecords(records: Iterable<CategoryRecord<R, C>>):
        SafeEmbedMessageBuilder {
    return addFields(
        records.map { cr -> cr.copy(categories = cr.categories.sortedBy { it as? Comparable<Any> }.toCollection(LinkedHashSet())) }
            .sortedBy { it.categories.first() as? Comparable<Any> }
            .map { (record, categories) ->
                EmbedCreateFields.Field.of(
                    categories.joinToString(", ") { it.displayName },
                    record?.toDisplayString(DisplayContext(StringFormat.MARKDOWN, categories.toList())) ?: "none",
                    true
                )
            }
    )
}

fun <R : Record<C>, C : Category> SafeEmbedMessageBuilder.embedRecords(records: Iterable<CategoryRecord<R, C>>):
        SafeEmbedMessageBuilder {
    return this.addFields(
        records.map { (record, categories) ->
            EmbedCreateFields.Field.of(
                if (categories.isNotEmpty()) categories.joinToString(", ") { it.displayName } else "\u200B", // NBSP
                record.toDisplayString(DisplayContext.markdown()),
                true
            )
        }
    )
}

fun InteractionCreateEvent.user(): User =
    this.interaction.member.map { it as User }.orElse(this.interaction.user)

fun InteractionReplyEditMono.clear() = withContentOrNull(null).withComponentsOrNull(null).withEmbedsOrNull(null)

fun InteractionCreateEvent.editReplyWithFailure(message: String?) =
    editReply().withEmbeds(
        EmbedCreateSpec.builder()
            .title("Failed")
            .color(Colors.FAILURE)
            .description(message?.truncateWithEllipsis(Limits.DESCRIPTION) ?: "Something went wrong")
            .build()
    ).then()

fun String.truncateWithEllipsis(maxLength: Int) = if (length > maxLength) substring(0, maxLength - 1) + "…" else this

fun <K, V, C : MutableCollection<V>> MutableMap<K, C>.add(key: K, values: C) {
    if (containsKey(key)) {
        getValue(key) += values
    } else {
        put(key, values)
    }
}

inline fun <T : Closeable?, U : Closeable?, R> Pair<T, U>.use(block: (T, U) -> R): R {
    try {
        return block(first, second)
    } finally {
        try {
            first?.close()
        } catch (closeException: Throwable) {
        }
        try {
            second?.close()
        } catch (closeException: Throwable) {
        }
    }
}

fun String?.orEmpty(prefix: String = "", suffix: String = "") = this?.let { prefix + it + suffix } ?: ""

fun String.ensurePrefix(prefix: String) = if (startsWith(prefix)) this else prefix + this

fun <T> Iterable<T>.productOf(selector: (T) -> Int) = fold(1) {acc, t -> acc * selector(t) }

fun isValidLink(string: String): Boolean {
    return try {
        val connection = URL(string).openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.setRequestProperty("Accept", "*/*")
        connection.responseCode in (200 until 400) // accept all redirects as well
    } catch (e: Exception) {
        false
    }
}