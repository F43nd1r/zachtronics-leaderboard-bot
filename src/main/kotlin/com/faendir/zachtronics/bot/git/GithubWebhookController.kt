package com.faendir.zachtronics.bot.git

import com.faendir.zachtronics.bot.config.GitProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.util.Base64Utils
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


@RestController
class GithubWebhookController(private val repositories: List<GitRepository>, private val gitProperties: GitProperties, private val objectMapper: ObjectMapper) {
    companion object {
        private val logger = LoggerFactory.getLogger(GithubWebhookController::class.java)
    }

    @PostMapping(path = ["/push"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun reportPush(@RequestBody payloadString: String, @RequestHeader(name = "X-Hub-Signature-256") signature: String) {
        if (signature != hash(payloadString)) {
            logger.warn("Received webhook call with invalid signature")
            return
        }
        val payload = objectMapper.readValue<Payload>(payloadString)
        if (payload.ref == "refs/heads/master" && payload.pusher.email != "zachtronics-leaderboard-bot@faendir.com") {
            val repository = repositories.find { it.url.equals(payload.repository.git_url, ignoreCase = true) }
            repository?.invalidate()
        }
    }

    private fun hash(payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(gitProperties.webhookSecret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return "sha256=" + Base64Utils.encode(mac.doFinal(payload.toByteArray(Charsets.UTF_8)))
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