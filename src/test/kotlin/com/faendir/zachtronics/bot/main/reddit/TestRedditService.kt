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

package com.faendir.zachtronics.bot.main.reddit

import com.faendir.zachtronics.bot.reddit.Comment
import com.faendir.zachtronics.bot.reddit.RedditService
import com.faendir.zachtronics.bot.reddit.Subreddit
import com.faendir.zachtronics.bot.utils.Forest
import java.io.File
import javax.annotation.PreDestroy

open class TestRedditService(private val directory: File) : RedditService {
    override fun getWikiPage(subreddit: Subreddit, page: String): String {
        return File(directory, "${subreddit.id}/wiki/$page.md").takeIf { it.exists() }?.readText() ?: ""
    }

    override fun updateWikiPage(subreddit: Subreddit, page: String, content: String, reason: String) {
        File(directory, "${subreddit.id}/wiki/$page.md").writeText(content)
    }

    override fun findCommentsOnPost(subreddit: Subreddit, title: String): Forest<Comment> {
        return Forest(emptyList())
    }

    override fun getModerators(subreddit: Subreddit): List<String> {
        return File(directory, "${subreddit.id}/moderators.txt").takeIf { it.exists() }?.readLines()?.filter { it.isNotBlank() } ?: emptyList()
    }

    override fun reply(comment: Comment, text: String) {
        //doesn't need to do anything in tests, right?
    }

    override fun myUsername(): String {
        return "zachtronics-bot-test"
    }

    @PreDestroy
    fun cleanup() {
        directory.deleteRecursively()
    }
}