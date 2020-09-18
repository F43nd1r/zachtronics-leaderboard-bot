package com.faendir.zachtronics.bot.reddit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dean.jraw.references.SubredditReference
import java.io.IOException
import java.net.URL

fun SubredditReference.moderators(): List<String> {
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
data class Moderator(val name: String, @SerialName("mod_permissions") val modPermissions: List<String>, @SerialName("rel_id") val relId: String, val id: String)
