package com.faendir.zachtronics.bot.utils

import com.faendir.zachtronics.bot.model.Puzzle

inline fun <T, R> Collection<T>.ifNotEmpty(block: (Collection<T>) -> R): R? = takeIf { it.isNotEmpty() }?.let(block)

inline fun <reified T> Iterable<*>.findInstance(): T? = filterIsInstance<T>().firstOrNull()

inline fun <reified T> Iterable<*>.findInstance(block: (T) -> Unit) = findInstance<T>()?.let(block)

fun <T : Map.Entry<K, V>, K, V> Iterable<T>.toMap() = associate { it.key to it.value }

fun <T> Iterable<T>.plusIf(condition: Boolean, element: T): List<T> = if (condition) plus(element) else toList()

private val wordSeparator = Regex("[\\s-/,:]+")

fun <P : Puzzle> Array<P>.getSingleMatchingPuzzle(name: String): P {
    val matches = asIterable().fuzzyMatch(name) { it.displayName }
    return when (val size = matches.size) {
        0 -> throw IllegalArgumentException("I did not recognize the puzzle \"$name\".")
        1 -> matches.first()
        in 2..5 -> throw IllegalArgumentException("your request for \"$name\" was not precise enough. Use one of:\n${matches.joinToString("\n") { "- *${it.displayName}*" }}")
        else -> throw IllegalArgumentException("your request for \"$name\" was not precise enough. $size matches.")
    }
}

fun <P> Iterable<P>.fuzzyMatch(search: String, extractName: (P) -> String): List<P> {
    return filter { puzzle ->
        val words = search.split(wordSeparator).toMutableList()
        val elementWords = extractName(puzzle).split(wordSeparator)
        elementWords.forEach {
            if (it.contains(words.first(), ignoreCase = true)) words.removeFirst()
            if (words.isEmpty()) return@filter true
        }
        if (search.length <= 4 && search.length == elementWords.size) {
            //check abbreviation
            search.foldIndexed(true) { index, acc, char -> acc && elementWords[index].startsWith(char, ignoreCase = true) }
        } else {
            false
        }
    }
}