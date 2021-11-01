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

package com.faendir.zachtronics.bot.model

enum class StringFormat(val separator: Char) {
    PLAIN_TEXT('/'),
    MARKDOWN('/'),
    FILE_NAME('-')
}

data class DisplayContext<C : Category>(val format: StringFormat, val categories: List<C>? = null) {
    constructor(format: StringFormat, category: C) : this(format, listOf(category))
    val separator by format::separator

    companion object {
        @JvmStatic
        fun <C : Category> plainText() = DisplayContext<C>(StringFormat.PLAIN_TEXT)
        @JvmStatic
        fun <C : Category> markdown() = DisplayContext<C>(StringFormat.MARKDOWN)
        @JvmStatic
        fun <C : Category> fileName() = DisplayContext<C>(StringFormat.FILE_NAME)
    }
}