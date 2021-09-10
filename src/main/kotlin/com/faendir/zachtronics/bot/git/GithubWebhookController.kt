package com.faendir.zachtronics.bot.git

import com.faendir.zachtronics.bot.config.GitProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.hash.Hashing
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController


@RestController
class GithubWebhookController(private val repositories: List<GitRepository>, private val gitProperties: GitProperties, private val objectMapper: ObjectMapper) {
    companion object {
        private val logger = LoggerFactory.getLogger(GithubWebhookController::class.java)
    }

    @PostMapping(path = ["/push"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun reportPush(@RequestBody payloadString: String, @RequestHeader(name = "X-Hub-Signature-256") signature: String) {
        if (signature.removePrefix("sha256=") != hash(payloadString)) {
            logger.warn("Received webhook call with invalid signature")
            return
        }
        val payload = objectMapper.readValue<Payload>(payloadString)
        if (payload.ref == "refs/heads/master") {
            val repository = repositories.find { it.url.equals(payload.repository.clone_url, ignoreCase = true) }
            if (repository != null) {
                logger.info("invalidating ${repository.name}")
                repository.updateRemoteHash(payload.headCommit.id)
            } else {
                logger.warn("received webhook for unknown repository ${payload.repository.clone_url}")
            }
        }
    }

    private fun hash(payload: String): String {
        return Hashing.hmacSha256(gitProperties.webhookSecret.toByteArray(Charsets.UTF_8)).hashString(payload, Charsets.UTF_8).toString()
    }
}

data class Payload(
    val ref: String,
    val pusher: User,
    val repository: Repository,
    val headCommit: Commit
)

data class Commit(
    val id: String,
    val author: User,
)

data class User(
    val name: String,
    val email: String,
)

data class Repository(
    val git_url: String,
    val clone_url: String,
)