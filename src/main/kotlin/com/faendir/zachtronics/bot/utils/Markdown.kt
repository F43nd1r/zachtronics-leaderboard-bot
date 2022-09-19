/*
 * Copyright (c) 2022
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

object Markdown {
    @JvmStatic
    @JvmOverloads
    fun link(text: String, link: String, embed: Boolean = true) = "[$text](${if (embed) link else "<$link>"})"

    @JvmStatic
    @JvmOverloads
    fun linkOrText(text: String, link: String?, embed: Boolean = true) = link?.let { link(text, it, embed) } ?: text

    @JvmStatic
    fun fileLinkOrEmpty(link: String?) = link?.let { "${link("\uD83D\uDCC4", it)} " } ?: "" // 📄
}