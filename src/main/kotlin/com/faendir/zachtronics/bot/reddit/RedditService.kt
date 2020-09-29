package com.faendir.zachtronics.bot.reddit

import com.faendir.zachtronics.bot.utils.Forest
import java.util.*

interface RedditService {
    fun getWikiPage(subreddit: Subreddit, page: String): String
    fun updateWikiPage(subreddit: Subreddit, page: String, content: String, reason: String)
    fun findCommentsOnPost(subreddit: Subreddit, title: String): Forest<Comment>
    fun getModerators(subreddit: Subreddit) : List<String>
    fun reply(comment: Comment, text: String)
    fun myUsername(): String
}

data class Comment(val id: String, val body: String?, val author: String, val created: Date, val edited: Date?)