package com.faendir.zachtronics.bot.reddit

import com.faendir.zachtronics.bot.utils.Forest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import javax.annotation.PreDestroy

open class TestRedditService(private val directory: File) : RedditService {
    override fun getWikiPage(subreddit: Subreddit, page: String): String {
        return File(directory, "${subreddit.id}/wiki/$page.md").takeIf { it.exists() }?.readText() ?: ""
    }

    override fun updateWikiPage(subreddit: Subreddit, page: String, content: String, reason: String) {
        File(directory, "${subreddit.id}/wiki/$page.md").writeText(page)
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