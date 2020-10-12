package com.faendir.zachtronics.bot.imgur

import com.faendir.zachtronics.bot.config.ImgurProperties
import com.roxstudio.utils.CUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

@Service
class ProductionImgurService(private val imgurProperties: ImgurProperties) : ImgurService {
    companion object {
        private const val TEN_MB = 10 * 1000 * 1000
        private val logger = LoggerFactory.getLogger(ProductionImgurService::class.java)
        private val whitelistedHosts = listOf("i.imgur.com", "youtu.be", "youtube.com", "cdn.discordapp.com")
    }

    private val ffmpeg = FFmpegExecutor(FFmpeg("/usr/bin/ffmpeg"), FFprobe("/usr/bin/ffprobe"))

    override fun tryRehost(link: String): String {
        val host = URI(link).host
        if(whitelistedHosts.any { host.contains(it, ignoreCase = true) }) return link
        return try {
            val filename = link.substringAfterLast("/")
            val input = File.createTempFile(filename.substringBeforeLast("."), ".${filename.substringAfterLast(".")}")
            if (link.startsWith("https://od.lk")) {
                //specialcase stupid opendrive links
                val connection = URL(link).openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = false
                connection.connect()
                val downloadLink = connection.getHeaderField("Location").removeSuffix("?inline=1")
                input.writeBytes(CUrl(downloadLink).exec())
                val converted = convertToVideo(input)
                upload(converted)
            } else {
                input.writeBytes(CUrl(link).exec())
                upload(input)
            }
        } catch (e: Throwable) {
            logger.info("Rehosting failed for $link", e)
            link
        }
    }

    private fun upload(file: File): String {
        val cUrl = when (file.extension) {
            "gif", "gifv" -> if (file.length() >= TEN_MB) videoUpload(convertToVideo(file)) else imageUpload(file)
            "png", "jpg" -> imageUpload(file)
            "mp4", "webm" -> videoUpload(file)
            else -> throw IllegalArgumentException("Unsupported file type ${file.extension}")
        }
        val string = cUrl.exec().decodeToString()
        val response = Json { ignoreUnknownKeys = true }.decodeFromString<Response>(string)
        return response.data?.link?.let { /*when uploading webm imgur doesn't return a file ending*/if (it.endsWith(".")) "${it}mp4" else it }
            ?: throw IOException("JSON response did not contain image link. $string")
    }

    private fun videoUpload(file: File): CUrl = CUrl("https://api.imgur.com/3/upload").basicUploadOptions().form("video", CUrl.FileIO(file))

    private fun imageUpload(file: File): CUrl = CUrl("https://api.imgur.com/3/image").basicUploadOptions().form("image", CUrl.FileIO(file))

    private fun CUrl.basicUploadOptions(): CUrl = opt("--location", "--request", "POST", "--header", "'Authorization: Client-ID ${imgurProperties.clientId}'")

    private fun convertToVideo(gif: File): File {
        val output = File.createTempFile(gif.nameWithoutExtension, ".webm")
        val builder = FFmpegBuilder().setInput(gif.canonicalPath)
            .overrideOutputFiles(true)
            .addOutput(output.canonicalPath)
            .setFormat("webm")
            .setVideoCodec("libvpx")
            .setConstantRateFactor(10.0)
            .setVideoBitRate(8 * 1024 * 1024)
            .addExtraArgs("-auto-alt-ref", "0")
            .done()
        ffmpeg.createJob(builder).run()
        return output
    }

    @Serializable
    private data class Response(val data: Data?)

    @Serializable
    private data class Data(val link: String)
}