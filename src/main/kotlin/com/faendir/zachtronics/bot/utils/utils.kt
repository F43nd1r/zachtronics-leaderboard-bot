package com.faendir.zachtronics.bot.utils

import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.utils.Result.Companion.parseFailure
import com.faendir.zachtronics.bot.utils.Result.Companion.success
import net.dv8tion.jda.api.entities.Message

inline fun <T, R> Collection<T>.ifNotEmpty(block: (Collection<T>) -> R): R? = takeIf { it.isNotEmpty() }?.let(block)

inline fun <reified T> Iterable<*>.findInstance(): T? = filterIsInstance<T>().firstOrNull()

inline fun <reified T> Iterable<*>.findInstance(block: (T) -> Unit) = findInstance<T>()?.let(block)

fun <T : Map.Entry<K, V>, K, V> Iterable<T>.toMap() = map { it.key to it.value }.toMap()

fun <T> Iterable<T>.plusIf(condition: Boolean, element: T): List<T> = if (condition) plus(element) else toList()

private val wordSeparator = Regex("[\\s-/,:]+")

fun <P : Puzzle> Array<P>.getSingleMatchingPuzzle(name: String): Result<P> {
    val matches = filter { puzzle ->
        val words = name.split(wordSeparator).toMutableList()
        val puzzleWords = puzzle.displayName.split(wordSeparator)
        puzzleWords.forEach {
            if (it.contains(words.first(), ignoreCase = true)) words.removeFirst()
            if (words.isEmpty()) return@filter true
        }
        if (name.length <= 4 && name.length == puzzleWords.size) {
            //check abbreviation
            name.foldIndexed(true) { index, acc, char -> acc && puzzleWords[index].startsWith(char, ignoreCase = true) }
        } else {
            false
        }
    }
    return when (val size = matches.size) {
        0 -> parseFailure("I did not recognize the puzzle \"$name\".")
        1 -> success(matches.first())
        in 2..5 -> parseFailure("your request for \"$name\" was not precise enough. Use one of:\n${matches.joinToString("\n") { it.displayName }}")
        else -> parseFailure("your request for \"$name\" was not precise enough. $size matches.")
    }
}

fun Message.match(regex: Regex): Result<MatchResult> {
    return regex.matchEntire(contentRaw)?.let { success(it) } ?: parseFailure("I could not parse your command. Type `!help` to see the syntax.")
}