package com.faendir.zachtronics.bot.reddit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dean.jraw.references.SubredditReference
import java.net.URL

fun SubredditReference.moderators(): List<String> {
    val response: Response = Json { ignoreUnknownKeys = true }.decodeFromString(URL("https://www.reddit.com/r/$subreddit/about/moderators.json").readText())
    check(response.kind == "UserList")
    return response.data.children.map { it.name }
}

@Serializable
private data class Response(val kind: String, val data: ModeratorData)

@Serializable
private data class ModeratorData(val children: List<Moderator>)

@Serializable
data class Moderator(val name: String, @SerialName("mod_permissions") val modPermissions: List<String>, @SerialName("rel_id") val relId: String, val id: String)
