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

package com.faendir.zachtronics.bot.reddit

import com.faendir.zachtronics.bot.utils.Forest
import java.util.*

interface RedditService {
    fun getWikiPage(subreddit: Subreddit, page: String): String
    fun updateWikiPage(subreddit: Subreddit, page: String, content: String, reason: String)
    fun findCommentsOnPost(subreddit: Subreddit, title: String): Forest<Comment>
    fun getModerators(subreddit: Subreddit): List<String>
    fun reply(comment: Comment, text: String)
    fun myUsername(): String
}

data class Comment(val id: String, val body: String?, val author: String, val created: Date, val edited: Date?)