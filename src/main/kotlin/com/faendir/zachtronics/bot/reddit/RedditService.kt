package com.faendir.zachtronics.bot.reddit

import com.faendir.zachtronics.bot.config.GitProperties
import com.faendir.zachtronics.bot.config.RedditProperties
import com.faendir.zachtronics.bot.git.GitRepository
import net.dean.jraw.RedditClient
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import org.springframework.stereotype.Service

@Service
class RedditService(redditProperties: RedditProperties, gitProperties: GitProperties) : GitRepository(gitProperties, "om-wiki", "https://github.com/F43nd1r/OM-wiki.git") {
    internal val reddit: RedditClient = OAuthHelper.automatic(OkHttpNetworkAdapter(UserAgent("bot", "com.faendir.zachtronics.bot", "1.0", redditProperties.username)),
        Credentials.script(redditProperties.username, redditProperties.password, redditProperties.clientId, redditProperties.accessToken))

    fun subreddit(subreddit: Subreddit) = reddit.subreddit(subreddit.id)
}