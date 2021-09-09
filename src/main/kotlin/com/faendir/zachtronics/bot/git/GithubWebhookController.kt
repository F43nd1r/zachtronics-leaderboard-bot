package com.faendir.zachtronics.bot.git

import com.faendir.zachtronics.bot.config.GitProperties
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class GithubWebhookController(private val repositories: List<GitRepository>, private val gitProperties: GitProperties) {
    companion object {
        private val logger = LoggerFactory.getLogger(GithubWebhookController::class.java)
    }

    @PostMapping(path = ["/push"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun reportPush(@RequestBody payload: Payload, @RequestHeader(name = "X-Hub-Signature-256") secret: String) {
        if (secret != gitProperties.webhookSecret) {
            logger.warn("Received webhook call with invalid secret")
            return
        }
        if (payload.ref == "refs/heads/master" && payload.pusher.email != "zachtronics-leaderboard-bot@faendir.com") {
            val repository = repositories.find { it.url.equals(payload.repository.git_url, ignoreCase = true) }
            repository?.invalidate()
        }
    }
}

data class Payload(
    val ref: String,
    val pusher: Pusher,
    val repository: Repository,
)

data class Pusher(
    val name: String,
    val email: String
)

data class Repository(
    val git_url: String
)