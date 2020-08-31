package com.faendir.om.discord.reddit

import com.faendir.om.discord.config.GitProperties
import com.faendir.om.discord.config.RedditProperties
import com.faendir.om.discord.git.GitRepository
import net.dean.jraw.RedditClient
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.models.SubredditSort
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import org.eclipse.jgit.api.Git
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files

@Service
class RedditService(redditProperties: RedditProperties, gitProperties: GitProperties) : GitRepository(gitProperties, "om-wiki", "https://github.com/F43nd1r/OM-wiki.git") {
    internal val reddit: RedditClient = OAuthHelper.automatic(OkHttpNetworkAdapter(UserAgent("bot", "com.faendir.om.discord", "1.0", redditProperties.username)),
        Credentials.script(redditProperties.username, redditProperties.password, redditProperties.clientId, redditProperties.accessToken))

    fun hotPosts() = reddit.subreddit("opus_magnum").posts().sorting(SubredditSort.HOT).limit(5).build().asSequence().flatten()
}