package com.faendir.zachtronics.bot.om.reddit

import com.faendir.zachtronics.bot.main.git.GitRepository
import com.faendir.zachtronics.bot.main.reddit.Comment
import com.faendir.zachtronics.bot.main.reddit.RedditService
import com.faendir.zachtronics.bot.main.reddit.Subreddit
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.model.UpdateResult
import com.faendir.zachtronics.bot.om.model.*
import com.faendir.zachtronics.bot.utils.DateSerializer
import com.faendir.zachtronics.bot.utils.Forest
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.TextChannel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.time.Instant
import java.util.*

@Service
@EnableScheduling
class OmRedditPostScraper(private val redditService: RedditService, private val jda: JDA, private val opusMagnum: OpusMagnum,
                          @Qualifier("configRepository") private val gitRepo: GitRepository,
                          private val leaderboards: List<Leaderboard<OmCategory, OmScore, OmPuzzle, OmRecord>>) {
    companion object {
        private const val timestampFile = "om-reddit-scraper/last_update.json"
        private const val trustFile = "om-reddit-scraper/trusted_users.txt"
    }

    private lateinit var trustedUsers: List<String>
    val mainRegex = Regex("\\s*(?<puzzle>[^:]*)[:\\s]\\s*(?<scores>(\\[[^]]*]\\([^)]*\\)[,\\s]*)+)")
    val scoreRegex = Regex("\\[(?<score>[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?(/[\\d.]+[a-zA-Z]?)?)]\\((?<link>http[^,\\s]*)\\)[,\\s]*")

    private val moderators by lazy { redditService.getModerators(Subreddit.OPUS_MAGNUM) }

    init {
        gitRepo.access { trustedUsers = File(repo, trustFile).readLines().filter { !it.isBlank() }.map { it.trim() } }
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
        val lastUpdate: Date? = gitRepo.access { File(repo, timestampFile).takeIf { it.exists() }?.readText() }
            ?.let { Json.decodeFromString(DateSerializer, it) }
        val comments = redditService.findCommentsOnPost(Subreddit.OPUS_MAGNUM, "official record submission thread")
        val hasNewComment = comments.walkTrees().map { comment -> handleComment(lastUpdate, comments, comment) }.toList().any { it }
        if (hasNewComment) {
            gitRepo.access {
                val timestamp = File(repo, timestampFile)
                timestamp.writeText(Json.encodeToString(DateSerializer, Date.from(now)))
                add(timestamp)
                commitAndPush("timestamp update")
            }
        }
    }

    internal fun handleComment(lastUpdate: Date?, comments: Forest<Comment>, comment: Comment): Boolean {
        return if (lastUpdate == null || (comment.edited ?: comment.created).after(lastUpdate)) {
            comment.body?.let { body ->
                if (body != "[deleted]") {
                    if (trustedUsers.contains(comment.author)) {
                        parseComment(comment)
                    } else if (body.lines().any { mainRegex.matches(it) }) {
                        redditService.reply(comment,
                            "[BOT] sorry, you're not a trusted user. Wait for a moderator to reply with `!trust-score` or " + "`!trust-user`.")
                    }
                    if (body.trim() == "!trust-user" && moderators.contains(comment.author)) {
                        getNonBotParentComment(comments, comment)?.let { trust ->
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
                    if (body.trim() == "!trust-score" && moderators.contains(comment.author)) {
                        getNonBotParentComment(comments, comment)?.let { parseComment(it) }
                    }
                }
            }
            true
        } else {
            false
        }
    }

    private fun getNonBotParentComment(forest: Forest<Comment>, commentNode: Comment): Comment? {
        return forest.parentOf(commentNode)?.let { if (it.author == redditService.myUsername()) forest.parentOf(it) else it }
    }

    private fun parseComment(comment: Comment) {
        var update = false
        comment.body?.lines()?.forEach loop@{ line ->
            val command = mainRegex.matchEntire(line) ?: return@loop
            val puzzle = opusMagnum.parsePuzzle(command.groups["puzzle"]!!.value).onFailure { return@loop }
            scoreRegex.findAll(command.groups["scores"]!!.value).forEach inner@{ subCommand ->
                val score: OmScore = opusMagnum.parseScore(puzzle, subCommand.groups["score"]!!.value).onFailure { return@inner }
                val link = subCommand.groups["link"]!!.value
                val results = leaderboards.map { leaderboard ->
                    update = true
                    leaderboard.update(puzzle, OmRecord(score, link, comment.author))
                }
                val successes = results.filterIsInstance<UpdateResult.Success<*, *>>()
                if (successes.isNotEmpty()) {
                    sendDiscordMessage(opusMagnum.discordChannel, "New record by ${comment.author} on reddit: ${puzzle.displayName} ${
                        successes.flatMap { it.oldScores.keys }.map { it.displayName }
                    } ${score.toDisplayString()} (previously ${
                        successes.flatMap { it.oldScores.entries }.joinToString { "`${it.key.displayName} ${it.value?.toDisplayString() ?: "none"}`" }
                    }) $link")
                }
            }
        }
        if (update) {
            redditService.reply(comment, "[BOT] thanks, your submission(s) have been recorded!")
        }
    }

    private fun sendDiscordMessage(channel: String, message: String) {
        jda.guilds.forEach { guild -> guild.channels.filterIsInstance<TextChannel>().find { it.name == channel }?.sendMessage(message)?.queue() }
    }
}