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

import com.faendir.zachtronics.bot.reddit.RedditService
import com.faendir.zachtronics.bot.reddit.Subreddit
import java.io.File
import javax.annotation.PreDestroy

open class TestRedditService(private val directory: File) : RedditService {
    override fun getWikiPage(subreddit: Subreddit, page: String): String {
        return File(directory, "${subreddit.id}/wiki/$page.md").takeIf { it.exists() }?.readText() ?: ""
    }

    override fun updateWikiPage(subreddit: Subreddit, page: String, content: String, reason: String) {
        File(directory, "${subreddit.id}/wiki/$page.md").writeText(content)
    }

    override fun postInSubmission(submissionId: String, content: String) {}

    @PreDestroy
    fun cleanup() {
        directory.deleteRecursively()
    }
}