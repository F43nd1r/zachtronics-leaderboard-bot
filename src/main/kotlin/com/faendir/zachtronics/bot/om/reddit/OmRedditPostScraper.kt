/*
 * Copyright (c) 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.om.reddit

import com.faendir.discord4j.command.parse.SingleParseResult
import com.faendir.zachtronics.bot.discord.DiscordService
import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.reddit.Comment
import com.faendir.zachtronics.bot.reddit.RedditService
import com.faendir.zachtronics.bot.reddit.Subreddit
import com.faendir.zachtronics.bot.utils.DateSerializer
import com.faendir.zachtronics.bot.utils.Forest
import com.faendir.zachtronics.bot.utils.filterIsInstance
import com.faendir.zachtronics.bot.utils.getSingleMatchingPuzzle
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.channel.MessageChannel
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.time.Instant
import java.util.*
import javax.annotation.PostConstruct

@Service
@EnableScheduling
class OmRedditPostScraper(
    private val redditService: RedditService, private val discordClient: GatewayDiscordClient,
    @Qualifier("configRepository") private val gitRepo: GitRepository,
    private val leaderboards: List<Leaderboard<OmCategory, OmPuzzle, OmRecord>>
) {
    companion object {
        private const val timestampFile = "om-reddit-scraper/last_update.json"
        private const val trustFile = "om-reddit-scraper/trusted_users.txt"
        private val logger = LoggerFactory.getLogger(DiscordService::class.java)
    }

    private lateinit var trustedUsers: List<String>
    val mainRegex = Regex("\\s*(?<puzzle>[^:]*)[:\\s]\\s*(?<scores>(\\[[^]]*]\\([^)]*\\)[,\\s]*)+)")
    val scoreRegex = Regex("\\[(?<score>[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?(/[\\d.]+[a-zA-Z]?)?)]\\((?<link>http[^,\\s]*)\\)[,\\s]*")

    private val moderators by lazy { redditService.getModerators(Subreddit.OPUS_MAGNUM) }

    @PostConstruct
    fun init() {
        gitRepo.access { trustedUsers = File(repo, trustFile).readLines().filter { it.isNotBlank() }.map { it.trim() } }
    }

    /*@PostConstruct
    fun importOldStuff() {
        val regex =
            Regex("!submit\\s+(?<puzzle>[^:]*)(:|\\s)\\s*(?<score>[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?(/[\\d.]+[a-zA-Z]?)?)(\\s+(?<link>http.*\\.(gif|gifv|mp4|webm)))?\\s*")
        redditService.accessRepo { _, repo ->
            File(repo, "commands.txt").readLines().forEach loop@{ line ->
                val command = regex.matchEntire(line.let {
                    if (!it.endsWith(".gif") && !it.endsWith(".gifv") && !it.endsWith(".mp4") && !it.endsWith(".webm")) {
                        "$it.gif"
                    } else it
                }) ?: return@loop
                val puzzleName = command.groups["puzzle"]!!.value
                val puzzles = Puzzle.findByName(puzzleName)
                if (puzzles.isEmpty() || puzzles.size > 1) {
                    return@loop
                }
                val puzzle = puzzles.first()
                val score: Score = Score.parse(puzzle, command.groups["score"]!!.value) ?: return@loop
                val leaderboardCategories = leaderboards.find(puzzle, score)
                if (leaderboardCategories.isEmpty()) {
                    return@loop
                }
                val link = command.groups["link"]!!.value
                leaderboardCategories.forEach { (leaderboard, categories) ->
                    leaderboard.update(
                        "nobody",
                        puzzle,
                        categories,
                        score,
                        link
                    )
                }
            }
        }
    }*/

    @Scheduled(fixedRate = 1000L * 60 * 60)
    fun pullFromReddit() {
        val now = Instant.now() //capture timestamp before processing
        val oldTimestamp = gitRepo.access { File(repo, timestampFile).takeIf { it.exists() }?.readText() }
        val lastUpdate = oldTimestamp?.let { Json.decodeFromString(DateSerializer, it) }
        val comments = redditService.findCommentsOnPost(Subreddit.OPUS_MAGNUM, "official record submission thread")
        val hasNewComments = comments.walkTrees().asIterable().map { comment -> handleComment(lastUpdate, comments, comment) }
        if (hasNewComments.any { it }) {
            gitRepo.access {
                val timestamp = File(repo, timestampFile)
                timestamp.writeText(Json.encodeToString(DateSerializer, Date.from(now)))
                add(timestamp)
                commitAndPush("timestamp update")
            }
        }
    }

    internal fun handleComment(lastUpdate: Date?, comments: Forest<Comment>, comment: Comment): Boolean {
        val isNewOrUpdated = lastUpdate == null || (comment.edited ?: comment.created).after(lastUpdate)
        if (isNewOrUpdated) {
            val body = comment.body
            try {
                if (body != null && body != "[deleted]") {
                    when {
                        trustedUsers.contains(comment.author) -> parseComment(comment)
                        body.lines().any { mainRegex.matches(it) } ->
                            redditService.reply(
                                comment,
                                "[BOT] sorry, you're not a trusted user. Wait for a moderator to reply with `!trust-score` or `!trust-user`."
                            )
                    }
                    when {
                        body.trim() == "!trust-user" && moderators.contains(comment.author) -> {
                            val trust = getNonBotParentComment(comments, comment)
                            if (trust != null) {
                                parseComment(trust)
                                trustedUsers = trustedUsers + trust.author
                                gitRepo.access {
                                    val file = File(repo, trustFile)
                                    file.appendText("\n${trust.author}\n")
                                    add(file)
                                    commitAndPush("added trusted user \"${trust.author}\"")
                                }
                            }
                        }
                        body.trim() == "!trust-score" && moderators.contains(comment.author) -> getNonBotParentComment(
                            comments,
                            comment
                        )?.let { parseComment(it) }
                    }
                }
            } catch (e: Exception) {
                logger.warn("Failed to parse comment $body", e)
            }
        }
        return isNewOrUpdated
    }

    private fun getNonBotParentComment(forest: Forest<Comment>, commentNode: Comment): Comment? {
        return forest.parentOf(commentNode)?.let { if (it.author == redditService.myUsername()) forest.parentOf(it) else it }
    }

    private fun parseComment(comment: Comment) {
        if (comment.body != null) {
            var foundSubmission = false
            for (line in comment.body.lines()) {
                val match = mainRegex.matchEntire(line) ?: continue
                val puzzleResult = OmPuzzle.values().getSingleMatchingPuzzle(match.groups["puzzle"]!!.value)
                val puzzle = if (puzzleResult is SingleParseResult.Success) puzzleResult.value else continue
                val scores = match.groups["scores"]!!.value
                for (subCommand in scoreRegex.findAll(scores)) {
                    val score = OmScore.parse(puzzle, subCommand.groups["score"]!!.value)
                    val link = subCommand.groups["link"]!!.value
                    val results = leaderboards.map { it.update(puzzle, OmRecord(score, link, comment.author)) }
                    val successes = results.filterIsInstance<UpdateResult.Success>()
                    if (successes.isNotEmpty()) {
                        sendDiscordMessage(
                            "opus-magnum", "New record by ${comment.author} on reddit: ${puzzle.displayName} ${
                                successes.flatMap { it.oldRecords.keys }.map { it.displayName }
                            } ${score.toDisplayString()} (previously ${
                                successes.flatMap { it.oldRecords.entries }
                                    .joinToString { "`${it.key.displayName} ${it.value?.score?.toDisplayString() ?: "none"}`" }
                            }) $link")
                        foundSubmission = true
                    }
                }
            }
            if (foundSubmission) {
                redditService.reply(comment, "[BOT] thanks, your submission(s) have been recorded!")
            }
        }
    }

    private fun sendDiscordMessage(channel: String, message: String) {
        discordClient.guilds.flatMap { it.channels }.filter { it.name == channel }.filterIsInstance<MessageChannel>().subscribe {
            it.createMessage(message)
        }
    }
}