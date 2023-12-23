/*
 * Copyright (c) 2023
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

package com.faendir.zachtronics.bot.git

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.hash.HashFunction
import io.swagger.v3.oas.annotations.Hidden
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Hidden
@RestController
class GithubWebhookController(private val repositories: List<GitRepository>, private val hashFunction: HashFunction, private val objectMapper: ObjectMapper) {
    companion object {
        private val logger = LoggerFactory.getLogger(GithubWebhookController::class.java)
    }

    @PostMapping(path = ["/push"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun reportPush(
        @RequestBody payloadString: String,
        @RequestHeader(name = "X-Hub-Signature-256") signature: String,
        @RequestHeader(name = "X-GitHub-Event") event: String
    ) {
        if (signature.removePrefix("sha256=") != hash(payloadString)) {
            logger.warn("Received webhook call with invalid signature")
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
        if (event == "push") {
            val payload = objectMapper.readValue<Payload>(payloadString)
            if (payload.ref == "refs/heads/master") {
                val repository = repositories.find { it.url.equals(payload.repository.clone_url, ignoreCase = true) }
                if (repository != null) {
                    logger.debug("invalidating ${repository.name}")
                    repository.updateRemoteHash(payload.headCommit.id)
                } else {
                    logger.warn("received webhook for unknown repository ${payload.repository.clone_url}")
                }
            }
        } else if (event == "ping") {
            logger.info("Received ping")
        } else {
            logger.warn("received unknown webhook event $event")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }

    private fun hash(payload: String): String {
        return hashFunction.hashString(payload, Charsets.UTF_8).toString()
    }
}

data class Payload(
    val ref: String,
    val pusher: User,
    val repository: Repository,
    @JsonProperty("head_commit")
    val headCommit: Commit,
)

data class Commit(
    val id: String,
    val author: User,
)

data class User(
    val name: String,
    val email: String,
)

@Suppress("PropertyName")
data class Repository(
    val git_url: String,
    val clone_url: String,
)