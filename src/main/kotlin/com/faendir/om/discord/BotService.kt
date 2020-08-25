package com.faendir.om.discord

import com.roxstudio.utils.CUrl
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.stereotype.Service
import java.io.File
import java.text.DecimalFormat
import javax.annotation.PreDestroy

@Service
class BotService(private val jdaProperties: JdaProperties, private val gitService: GitService) : ListenerAdapter() {
    private lateinit var jda: JDA
    private val numberFormat = DecimalFormat("0.#")

    fun start() {
        jda = JDABuilder.createLight(jdaProperties.token, GatewayIntent.GUILD_MESSAGES)
            .addEventListeners(this)
            .build()
        jda.awaitReady()
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        val message = event.message.contentRaw
        if (message.startsWith("!height") || message.startsWith("!width")) {
            val regex =
                Regex("!(?<hw>height|width)\\s+(?<name>[^:]*)(:|\\s)\\s*(?<score>[\\d.]+/\\d+/\\d+)\\s+(?<link>http.*\\.(?<format>gif|mp4|webm))")
            val matches = regex.findAll(message).toList()
            if (matches.isEmpty()) {
                event.channel.sendMessage("${event.author.asMention} sorry, I did not understand your command.\nPlease use the format `!<width|height> <name>:<width-or-height-score>/<cycles>/<cost> <link>`.")
                    .mention(event.author).queue()
            }
            matches.forEach { result ->
                val isHeight = result.groups["hw"]!!.value == "height"
                val name = result.groups["name"]!!.value
                val score = result.groups["score"]!!.value.split('/').map { it.toDouble() }
                val link = result.groups["link"]!!.value
                val format = result.groups["format"]!!.value
                gitService.update(name, event.author.name) { directory ->
                    val normalizedName = name.replace(Regex("\\s"), "_")
                    val puzzleDir = File(directory, "gif").walkTopDown().filter { it.isDirectory }
                        .find { it.name.contains(normalizedName) }
                    if (puzzleDir != null) {
                        val prefix = if (isHeight) "H" else "W"
                        val existingFile = puzzleDir.listFiles()?.find { it.name.startsWith(prefix) }
                        val existingScore = existingFile?.getScore()
                        if (existingScore == null || score.isBetterScoreThan(existingScore)) {
                            try {
                                val img = CUrl(link).exec()
                                File(puzzleDir, "${prefix}_${score.toScoreString("_")}.${format}")
                                    .writeBytes(img)
                                existingFile?.delete()
                                Runtime.getRuntime().exec("/bin/bash ./generate.sh", null, directory).waitFor()
                                event.channel.sendMessage(
                                    "${event.author.asMention} thanks, the site will be updated shortly with $name ${prefix}${
                                        score.toScoreString("/")
                                    } " + if (existingScore != null) "(previously ${prefix}${
                                        existingScore.toScoreString("/")
                                    })." else "."
                                ).mention(event.author).queue()
                            } catch (e: Exception) {
                                event.channel.sendMessage("${event.author.asMention} sorry, I could not load the file at $link.")
                                    .mention(event.author).queue()
                            }
                        } else {
                            event.channel.sendMessage(
                                "${event.author.asMention} sorry, there is already a better score for \"$name\": ${prefix}${
                                    existingScore.toScoreString("/")
                                }."
                            )
                                .mention(event.author).queue()
                        }
                    } else {
                        event.channel.sendMessage("${event.author.asMention} sorry, I did not recognize the puzzle \"$name\".")
                            .mention(event.author).queue()
                    }
                }
            }
        }
    }

    private fun List<Double>.toScoreString(separator: String) = joinToString(separator) { numberFormat.format(it) }

    private fun File.getScore(): List<Double> {
        return Regex("[HW]_(?<hw>[\\d.]+)_(?<cycles>\\d+)_(?<cost>\\d+).*").matchEntire(name)!!.let {
            listOf(
                it.groups["hw"]!!.value.toDouble(),
                it.groups["cycles"]!!.value.toDouble(),
                it.groups["cost"]!!.value.toDouble()
            )
        }
    }

    private fun List<Double>.isBetterScoreThan(other: List<Double>): Boolean {
        check(this.size == 3)
        check(other.size == 3)
        return this[0] < other[0] || (this[0] == other[0] && (this[1] < other[1] || (this[1] == other[1] && this[2] <= other[2])))
    }

    @PreDestroy
    fun stop() {
        jda.shutdown()
    }
}