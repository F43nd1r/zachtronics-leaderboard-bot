package com.faendir.om.discord.reddit

import net.dean.jraw.RedditClient
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import org.eclipse.jgit.api.Git
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files

@Service
class RedditService(redditProperties: RedditProperties) {
    private val repo = Files.createTempDirectory("om-wiki").toFile()
    internal val reddit: RedditClient

    init {
        val userAgent = UserAgent("bot", "com.faendir.om.discord", "1.0", redditProperties.username)
        val credentials = Credentials.script(
            redditProperties.username,
            redditProperties.password,
            redditProperties.clientId,
            redditProperties.accessToken
        )
        reddit = OAuthHelper.automatic(OkHttpNetworkAdapter(userAgent), credentials)
        synchronized(repo) {
            Git.cloneRepository()
                .setURI("https://github.com/F43nd1r/OM-wiki.git")
                .setDirectory(repo)
                .call()
        }
    }

    fun <T> accessRepo(access: (Git, File) -> T): T {
        return synchronized(repo) {
            Git.open(repo).use { git ->
                git.pull().setTimeout(120).call()
                access(git, repo)
            }
        }
    }
}