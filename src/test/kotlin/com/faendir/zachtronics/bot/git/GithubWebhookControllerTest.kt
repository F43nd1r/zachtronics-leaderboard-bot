/*
 * Copyright (c) 2022
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

import com.faendir.zachtronics.bot.testutils.expectRuns
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.hash.Hashing
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class GithubWebhookControllerTest {
    private val hashFunction = Hashing.sha256()
    private val gitRepository = mockk<GitRepository>(relaxed = true)
    private val objectMapper = jacksonObjectMapper()
    private val githubWebhookController = GithubWebhookController(listOf(gitRepository, mockk(relaxed = true)), hashFunction, objectMapper)

    @Test
    fun `should throw 401 on missing signature`() {
        expectThrows<ResponseStatusException> {
            githubWebhookController.reportPush("", "", "")
        }.get { status }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `should throw 401 on bad signature`() {
        expectThrows<ResponseStatusException> {
            githubWebhookController.reportPush("", "sha256=wrong", "")
        }.get { status }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `should throw 404 on unknown event type`() {
        expectThrows<ResponseStatusException> {
            githubWebhookController.reportPush("", "sha256=${hashFunction.hashString("", Charsets.UTF_8)}", "unknown-event")
        }.get { status }.isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should accept ping`() {
        expectRuns { githubWebhookController.reportPush("", "sha256=${hashFunction.hashString("", Charsets.UTF_8)}", "ping") }
    }

    @Test
    fun `should update git repository remote hash by url`() {
        val url = "https://github.com/test/test.git"
        every { gitRepository.url } returns url
        val user = User("test", "test@email.com")
        val payload = Payload(
            ref = "refs/heads/master",
            pusher = user,
            repository = Repository("git:github.com/test/test", url),
            headCommit = Commit("id", user)
        )
        val payloadString = objectMapper.writeValueAsString(payload)

        githubWebhookController.reportPush(payloadString, "sha256=${hashFunction.hashString(payloadString, Charsets.UTF_8)}", "push")

        verify { gitRepository.updateRemoteHash(payload.headCommit.id) }
    }
}