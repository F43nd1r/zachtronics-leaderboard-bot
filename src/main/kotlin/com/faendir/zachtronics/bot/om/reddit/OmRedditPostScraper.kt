package com.faendir.zachtronics.bot.om.reddit

import com.faendir.zachtronics.bot.main.git.GitRepository
import com.faendir.zachtronics.bot.main.reddit.Comment
import com.faendir.zachtronics.bot.main.reddit.RedditService
import com.faendir.zachtronics.bot.main.reddit.Subreddit
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.model.UpdateResult
import com.faendir.zachtronics.bot.om.model.*
import com.faendir.zachtronics.bot.utils.*
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.channel.MessageChannel
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3
import reactor.util.function.Tuples
import java.io.File
import java.time.Instant
import java.util.*
import kotlin.streams.asStream

@Service
@EnableScheduling
class OmRedditPostScraper(
    private val redditService: RedditService, private val discordClient: GatewayDiscordClient, private val opusMagnum: OpusMagnum,
    @Qualifier("configRepository") private val gitRepo: GitRepository,
    private val leaderboards: List<Leaderboard<OmCategory, OmScore, OmPuzzle, OmRecord>>
) {
    companion object {
        private const val timestampFile = "om-reddit-scraper/last_update.json"
        private const val trustFile = "om-reddit-scraper/trusted_users.txt"
    }

    private lateinit var trustedUsers: List<String>
    val mainRegex = Regex("\\s*(?<puzzle>[^:]*)[:\\s]\\s*(?<scores>(\\[[^]]*]\\([^)]*\\)[,\\s]*)+)")
    val scoreRegex = Regex("\\[(?<score>[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?(/[\\d.]+[a-zA-Z]?)?)]\\((?<link>http[^,\\s]*)\\)[,\\s]*")

    private val moderators by lazy { redditService.getModerators(Subreddit.OPUS_MAGNUM) }

    init {
        gitRepo.access { trustedUsers = File(repo, trustFile).readLines().filter { it.isNotBlank() }.map { it.trim() } }.block()
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
        gitRepo.access { File(repo, timestampFile).takeIf { it.exists() }?.readText() }
            .map { Json.decodeFromString(DateSerializer, it) }
            .flatMap { lastUpdate ->
                val comments = redditService.findCommentsOnPost(Subreddit.OPUS_MAGNUM, "official record submission thread")
                Flux.fromStream(comments.walkTrees().asStream())
                    .map { comment -> handleComment(lastUpdate, comments, comment) }
                    .collectList()
                    .map { it.any() }
                    .flatMap { hasNewComment ->
                        if (hasNewComment) {
                            gitRepo.access {
                                val timestamp = File(repo, timestampFile)
                                timestamp.writeText(Json.encodeToString(DateSerializer, Date.from(now)))
                                add(timestamp)
                                commitAndPush("timestamp update")
                            }
                        } else {
                            Mono.empty()
                        }
                    }
            }
            .subscribe()
    }

    internal fun handleComment(lastUpdate: Date?, comments: Forest<Comment>, comment: Comment): Mono<Boolean> {
        return if (lastUpdate == null || (comment.edited ?: comment.created).after(lastUpdate)) {
            Mono.fromCallable<String> { comment.body }.flatMap { body ->
                if (body != "[deleted]") {
                    Mono.`when`(when {
                        trustedUsers.contains(comment.author) -> parseComment(comment)
                        body.lines().any { mainRegex.matches(it) } -> Mono.fromCallable {
                            redditService.reply(
                                comment,
                                "[BOT] sorry, you're not a trusted user. Wait for a moderator to reply with `!trust-score` or `!trust-user`."
                            )
                        }
                        else -> Mono.empty()
                    }, when {
                        body.trim() == "!trust-user" && moderators.contains(comment.author) -> getNonBotParentComment(comments, comment).map { trust ->
                            parseComment(trust).and {
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
                        ).map { parseComment(it) }
                        else -> Mono.empty()
                    })
                } else {
                    Mono.empty()
                }.then(true.toMono())
            }
        } else {
            false.toMono()
        }
    }

    private fun getNonBotParentComment(forest: Forest<Comment>, commentNode: Comment): Mono<Comment> {
        return forest.toMono().kMapNotNull { it.parentOf(commentNode) }.map { if (it.author == redditService.myUsername()) forest.parentOf(it) else it }
    }

    private fun parseComment(comment: Comment): Mono<Void> {
        return if (comment.body != null) {
            Flux.fromIterable(comment.body.lines())
                .kMapNotNull { mainRegex.matchEntire(it) }
                .map { Tuples.of(opusMagnum.parsePuzzle(it.groups["puzzle"]!!.value), it.groups["scores"]!!.value) }
                .flatMapIterable { (puzzle, scores) -> scoreRegex.findAll(scores).map { Tuples.of(puzzle, it) }.asIterable() }
                .map { (puzzle, subCommand) ->
                    Tuples.of(puzzle, opusMagnum.parseScore(puzzle, subCommand.groups["score"]!!.value), subCommand.groups["link"]!!.value)
                }.flatMap { (puzzle, score, link) ->
                    leaderboards.toFlux().flatMap { it.update(puzzle, OmRecord(score, link, comment.author)) }.collectList()
                        .map { results: List<UpdateResult> ->
                            val successes = results.filterIsInstance<UpdateResult.Success>()
                            if (successes.isNotEmpty()) {
                                sendDiscordMessage(opusMagnum.discordChannel, "New record by ${comment.author} on reddit: ${puzzle.displayName} ${
                                    successes.flatMap { it.oldScores.keys }.map { it.displayName }
                                } ${score.toDisplayString()} (previously ${
                                    successes.flatMap { it.oldScores.entries }
                                        .joinToString { "`${it.key.displayName} ${it.value?.toDisplayString() ?: "none"}`" }
                                }) $link")
                            }
                        }.map { true }
                        .defaultIfEmpty(false)
                }.collectList()
                .map { it.any() }
                .ifTrue { redditService.reply(comment, "[BOT] thanks, your submission(s) have been recorded!") }
                .then()
        } else {
            Mono.empty()
        }
    }

    private fun sendDiscordMessage(channel: String, message: String) {
        discordClient.guilds.flatMap { it.channels }.filter { it.name == channel }.filterIsInstance<MessageChannel>().subscribe {
            it.createMessage(message)
        }
    }
}