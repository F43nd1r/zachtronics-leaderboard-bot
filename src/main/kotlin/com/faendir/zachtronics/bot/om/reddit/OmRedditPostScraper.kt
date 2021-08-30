package com.faendir.zachtronics.bot.om.reddit

import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.model.UpdateResult
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
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.channel.MessageChannel
import kotlinx.serialization.json.Json
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
        }
        return isNewOrUpdated
    }

    private fun getNonBotParentComment(forest: Forest<Comment>, commentNode: Comment): Comment? {
        return forest.parentOf(commentNode)?.let { if (it.author == redditService.myUsername()) forest.parentOf(it) else it }
    }

    private fun parseComment(comment: Comment) {
        if (comment.body != null) {
            val results = comment.body.lines()
                .mapNotNull { mainRegex.matchEntire(it) }
                .flatMap {
                    val puzzle = OmPuzzle.parse(it.groups["puzzle"]!!.value)
                    val scores = it.groups["scores"]!!.value
                    scoreRegex.findAll(scores).map { matchResult -> puzzle to matchResult }
                }.map { (puzzle, subCommand) ->
                    val score = OmScore.parse(puzzle, subCommand.groups["score"]!!.value)
                    val link = subCommand.groups["link"]!!.value
                    val results = leaderboards.map { it.update(puzzle, OmRecord(score, link, comment.author)) }
                    val successes = results.filterIsInstance<UpdateResult.Success>()
                    val hasSuccess = successes.isNotEmpty()
                    if (hasSuccess) {
                        sendDiscordMessage(
                            "opus-magnum", "New record by ${comment.author} on reddit: ${puzzle.displayName} ${
                                successes.flatMap { it.oldScores.keys }.map { it.displayName }
                            } ${score.toDisplayString()} (previously ${
                                successes.flatMap { it.oldScores.entries }
                                    .joinToString { "`${it.key.displayName} ${it.value?.toDisplayString() ?: "none"}`" }
                            }) $link")
                    }
                    hasSuccess
                }
            if (results.any { it }) {
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