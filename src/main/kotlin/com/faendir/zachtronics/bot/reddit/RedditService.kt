package com.faendir.zachtronics.bot.reddit

import com.faendir.zachtronics.bot.config.RedditProperties
import net.dean.jraw.RedditClient
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import net.dean.jraw.references.Referenceable
import org.springframework.stereotype.Service

@Service
class RedditService(redditProperties: RedditProperties) {
    private val reddit: RedditClient =
            OAuthHelper.automatic(OkHttpNetworkAdapter(UserAgent("bot", "com.faendir.zachtronics.bot", "1.0", redditProperties.username)),
                    Credentials.script(redditProperties.username, redditProperties.password, redditProperties.clientId, redditProperties.accessToken))

    fun subreddit(subreddit: Subreddit) = reddit.subreddit(subreddit.id)

    fun me() = reddit.me()

    fun <R : Referenceable<T>, T> toReference(r : R) : T = r.toReference(reddit)
}

fun <R : Referenceable<T>, T> R.toReference(redditService: RedditService) : T = redditService.toReference(this)