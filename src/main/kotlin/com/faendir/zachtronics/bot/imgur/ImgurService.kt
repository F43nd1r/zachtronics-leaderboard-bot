package com.faendir.zachtronics.bot.imgur

import com.faendir.zachtronics.bot.config.ImgurProperties
import com.roxstudio.utils.CUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException

@Service
class ImgurService(private val imgurProperties: ImgurProperties) {

    fun upload(file: File): String {
        val cUrl = when (file.extension) {
            "gif", "gifv" -> CUrl("https://api.imgur.com/3/image").opt("--location",
                "--request",
                "POST",
                "--header",
                "'Authorization: Client-ID ${imgurProperties.clientId}'").form("image", CUrl.FileIO(file))
            "mp4", "webm" -> CUrl("https://api.imgur.com/3/upload").opt("--location",
                "--request",
                "POST",
                "--header",
                "'Authorization: Client-ID 3e19c41d4f2e193'").form("video", CUrl.FileIO(file))
            else -> throw IllegalArgumentException("Unsupported file type ${file.extension}")
        }
        val response = Json { ignoreUnknownKeys = true }.decodeFromString<Response>(cUrl.exec().decodeToString())
        return response.data?.link ?: throw IOException("JSON response did not contain image link")
    }

    @Serializable
    private data class Response(val data: Data?)

    @Serializable
    private data class Data(val link: String)
}