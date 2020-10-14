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
    val matches = asIterable().fuzzyMatch(name) { it.displayName }
    return when (val size = matches.size) {
        0 -> parseFailure("I did not recognize the puzzle \"$name\".")
        1 -> success(matches.first())
        in 2..5 -> parseFailure("your request for \"$name\" was not precise enough. Use one of:\n${matches.joinToString("\n") { it.displayName }}")
        else -> parseFailure("your request for \"$name\" was not precise enough. $size matches.")
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

fun Message.match(regex: Regex): Result<MatchResult> {
    return regex.matchEntire(contentRaw)?.let { success(it) } ?: parseFailure("I could not parse your command. Type `!help` to see the syntax.")
}

fun Message.changeContent(content: String): Message = ChangedMessage(this, content)

private class ChangedMessage(wrap: Message, private val content: String) : Message by wrap {
    override fun getContentRaw(): String = content
}