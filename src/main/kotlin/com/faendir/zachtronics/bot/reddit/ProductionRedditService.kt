package com.faendir.zachtronics.bot.reddit

import com.faendir.zachtronics.bot.config.RedditProperties
import com.faendir.zachtronics.bot.utils.Forest
import com.faendir.zachtronics.bot.utils.MutableTree
import com.faendir.zachtronics.bot.utils.Tree
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dean.jraw.RedditClient
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.models.PublicContribution
import net.dean.jraw.models.SubredditSort
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import net.dean.jraw.references.CommentReference
import net.dean.jraw.tree.CommentNode
import org.springframework.stereotype.Service
import java.io.IOException
import java.net.URL

@Service
class ProductionRedditService(redditProperties: RedditProperties) : RedditService {
    private val reddit: RedditClient = OAuthHelper.automatic(OkHttpNetworkAdapter(UserAgent("bot",
        "com.faendir.zachtronics.bot",
        "1.0",
        redditProperties.username)),
        Credentials.script(redditProperties.username, redditProperties.password, redditProperties.clientId, redditProperties.accessToken))

    private fun subreddit(subreddit: Subreddit) = reddit.subreddit(subreddit.id)

    override fun getWikiPage(subreddit: Subreddit, page: String): String = subreddit(subreddit).wiki().page(page).content

    override fun updateWikiPage(subreddit: Subreddit, page: String, content: String, reason: String) {
        subreddit(subreddit).wiki().update(page, content, reason)
    }

    override fun findCommentsOnPost(subreddit: Subreddit, title: String): Forest<Comment> {
        return Forest(subreddit(subreddit).posts()
            .sorting(SubredditSort.HOT)
            .limit(5)
            .build()
            .asSequence()
            .flatten()
            .find { it.title.contains(title, ignoreCase = true) }
            ?.toReference(reddit)
            ?.comments()
            ?.map { it.toTree() } ?: emptyList())
    }

    override fun getModerators(subreddit: Subreddit): List<String> {
        var text: String? = null
        while (text == null) {
            try {
                text = URL("https://www.reddit.com/r/$subreddit/about/moderators.json").readText()
            } catch (e: IOException) {
                if (e.message?.contains("429") == true) {
                    //API overload, wait a bit
                    Thread.sleep(1000 * 60)
                } else throw e
            }
        }
        val response: Response = Json { ignoreUnknownKeys = true }.decodeFromString(text)
        check(response.kind == "UserList")
        return response.data.children.map { it.name }
    }

    @Serializable
    private data class Response(val kind: String, val data: ModeratorData)

    @Serializable
    private data class ModeratorData(val children: List<Moderator>)

    @Serializable
    data class Moderator(val name: String, @SerialName("mod_permissions") val modPermissions: List<String>, @SerialName("rel_id") val relId: String,
                         val id: String)

    private fun CommentNode<PublicContribution<*>>.toTree(): Tree<Comment> {
        val comment = subject
        return MutableTree(Comment(comment.id, comment.body, comment.author, comment.created, comment.edited), replies.map { it.toTree() }.toMutableList())
    }

    override fun reply(comment: Comment, text: String) {
        CommentReference(reddit, comment.id).reply(text)
    }

    override fun myUsername(): String = reddit.me().username
}