package com.faendir.zachtronics.bot.reddit

import com.faendir.zachtronics.bot.utils.Forest

class TestRedditService : RedditService {
    override fun getWikiPage(subreddit: Subreddit, page: String): String {
        TODO("Not yet implemented")
    }

    override fun updateWikiPage(subreddit: Subreddit, page: String, content: String, reason: String) {
        TODO("Not yet implemented")
    }

    override fun findCommentsOnPost(subreddit: Subreddit, title: String): Forest<Comment> {
        TODO("Not yet implemented")
    }

    override fun getModerators(subreddit: Subreddit): List<String> {
        TODO("Not yet implemented")
    }

    override fun reply(comment: Comment, text: String) {
        TODO("Not yet implemented")
    }

    override fun myUsername(): String {
        TODO("Not yet implemented")
    }
}