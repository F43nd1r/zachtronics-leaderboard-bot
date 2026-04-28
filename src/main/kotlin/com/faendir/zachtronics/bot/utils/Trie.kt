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

/**
 * @param T data type
 */
class Trie<T> {
    private class TrieNode<T>(
        val children: MutableMap<Char, TrieNode<T>> = mutableMapOf(),
        var data: MutableList<T> = mutableListOf()
    )

    private val root = TrieNode<T>()

    fun get(word: CharSequence): List<T>? {
        var current = root
        for (c in word) {
            current = current.children[c.uppercaseChar()] ?: return null
        }
        return current.data
    }

    fun put(word: CharSequence, data: T) {
        var current = root
        for (c in word) {
            current = current.children.getOrPut(c.uppercaseChar()) { TrieNode() }
        }
        current.data.add(data)
    }

    /**
     * Scans the query to find the longest matching prefix dictionary key.
     * @return Pair(values, endIdx)
     */
    fun findLongestPrefix(query: CharSequence, startIndex: Int = 0): Pair<List<T>, Int> {
        var current = root
        var longestMatchData: List<T> = emptyList()
        var longestMatchEnd = 0

        for (i in startIndex until query.length) {
            val c = query[i].uppercaseChar()
            // If the character isn't a child of the current node, we can't match further
            current = current.children[c] ?: break

            // If this node has data, it marks a valid dictionary word.
            // We overwrite our previous best match to ensure we get the *longest* one.
            if (current.data.isNotEmpty()) {
                longestMatchData = current.data
                longestMatchEnd = i
            }
        }
        return longestMatchData to longestMatchEnd + 1
    }
}
