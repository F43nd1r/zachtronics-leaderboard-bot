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

import com.faendir.zachtronics.bot.config.RedditProperties
import net.dean.jraw.RedditClient
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper

class ProductionRedditService(redditProperties: RedditProperties) : RedditService {
    private val reddit: RedditClient = OAuthHelper.automatic(
        OkHttpNetworkAdapter(
            UserAgent(
                "bot",
                "com.faendir.zachtronics.bot",
                "1.0",
                redditProperties.username
            )
        ),
        Credentials.script(redditProperties.username, redditProperties.password, redditProperties.clientId, redditProperties.accessToken)
    )

    private fun subreddit(subreddit: Subreddit) = reddit.subreddit(subreddit.id)

    override fun getWikiPage(subreddit: Subreddit, page: String): String = subreddit(subreddit).wiki().page(page).content

    override fun updateWikiPage(subreddit: Subreddit, page: String, content: String, reason: String) {
        val latinReason = reason.replace("\\P{InBasic_Latin}".toRegex(), "?") // reddit cries if the reason has strange chars
        subreddit(subreddit).wiki().update(page, content, latinReason)
    }

    override fun postInSubmission(submissionId: String, content: String) {
        reddit.submission(submissionId).reply(content)
    }
}