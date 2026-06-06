/*
 * Copyright (c) 2026
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

import java.net.HttpURLConnection
import java.net.URI
import java.util.*

private val wordSeparator = Regex("[\\s-/,:]+")

fun <P> Collection<P>.fuzzyMatch(search: String, name: P.() -> String): List<P> {
    return search.takeIf { it.isEmpty() }?.let { emptyList() }
        ?: find {
            // exact match
            it.name().equals(search, ignoreCase = true)
        }?.let { listOf(it) }
        ?: filter {
            //abbreviation
            val words = it.name().split(wordSeparator)
            search.length == words.size && words.zip(search.asIterable()).all { (word, char) -> word.startsWith(char, ignoreCase = true) }
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

fun String.truncateWithEllipsis(maxLength: Int) = if (length > maxLength) substring(0, maxLength - 1) + "…" else this

fun String.ifEmptyZeroWidthSpace() = ifEmpty { "\u200B" }

fun String?.orEmpty(prefix: String = "", suffix: String = "") = this?.let { prefix + it + suffix } ?: ""

inline fun <T> T.runIf(condition: Boolean, transform: T.() -> T): T = if (condition) transform() else this

inline fun <reified T : Enum<T>> newEnumSet(): EnumSet<T> = EnumSet.noneOf(T::class.java)

fun isValidLink(string: String): Boolean {
    return try {
        val url = URI(string).toURL()
        if (url.host.contains("reddit.com")) // the bot IP has been banned from reddit crawling, we just accept them
            return true
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.setRequestProperty("Accept", "*/*")
        connection.responseCode in (200 until 400) // accept all redirects as well
    } catch (_: Exception) {
        false
    }
}
