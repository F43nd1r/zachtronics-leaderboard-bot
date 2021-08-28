package com.faendir.zachtronics.bot.utils

import com.faendir.zachtronics.bot.model.Puzzle
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