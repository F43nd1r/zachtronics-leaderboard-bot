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

package com.faendir.zachtronics.bot

import com.faendir.zachtronics.bot.config.GitProperties
import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.mors.MorsService
import com.faendir.zachtronics.bot.reddit.RedditService
import com.faendir.zachtronics.bot.testutils.JGitNoExternalConfigReader
import com.faendir.zachtronics.bot.testutils.TestGitRepository
import com.faendir.zachtronics.bot.testutils.TestRedditService
import discord4j.core.GatewayDiscordClient
import discord4j.rest.RestClient
import io.mockk.every
import io.mockk.mockk
import org.eclipse.jgit.api.Git
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cloud.context.restart.RestartEndpoint
import org.springframework.context.annotation.Bean
import org.springframework.util.ResourceUtils
import reactor.core.publisher.Flux
import java.io.File
import java.nio.file.Files

@TestConfiguration
class TestConfiguration(private val gitProperties: GitProperties) {

    @Bean("cwRepository")
    fun cwRepository(): GitRepository {
        return createTestGitRepository("repositories/cw-leaderboard")
    }

    @Bean("fcRepository")
    fun fcRepository(): GitRepository {
        return createTestGitRepository("repositories/fc-leaderboard")
    }

    @Bean("fpRepository")
    fun fpRepository(): GitRepository {
        return createTestGitRepository("repositories/fp-leaderboard")
    }

    @Bean("ifRepository")
    fun ifRepository(): GitRepository {
        return createTestGitRepository("repositories/if-leaderboard")
    }

    @Bean("omLeaderboardRepository")
    fun omGithubPagesLeaderboardRepository(): GitRepository {
        return createTestGitRepository("repositories/om-leaderboard")
    }

    @Bean("scArchiveRepository")
    fun scArchiveRepository(): GitRepository {
        return createTestGitRepository("repositories/sc-archive")
    }

    @Bean("szRepository")
    fun szRepository(): GitRepository {
        return createTestGitRepository("repositories/sz-leaderboard")
    }

    @Bean
    fun redditService(): RedditService {
        return TestRedditService(extractResourceDirectory("reddit"))
    }

    @Bean
    fun discordClient(): GatewayDiscordClient {
        val client = mockk<GatewayDiscordClient>(relaxed = true)
        every { client.guilds } returns Flux.empty()
        val restClient = mockk<RestClient>(relaxed = true)
        every { client.restClient } returns restClient
        return client
    }

    @Bean
    fun morsService(): MorsService = mockk<MorsService>(relaxed = true)

    @Bean
    fun restartEndpoint() = mockk<RestartEndpoint>(relaxed = true)

    private fun createTestGitRepository(dir: String): GitRepository = createGitRepositoryFrom(extractResourceDirectory(dir), gitProperties)

    private fun extractResourceDirectory(dir: String): File {
        val resourceDir = ResourceUtils.getFile("classpath:$dir")
        val target = Files.createTempDirectory(resourceDir.name).toFile()
        resourceDir.copyRecursively(target)
        return target
    }

    companion object {
        init {
            JGitNoExternalConfigReader.install()
        }
    }
}

fun createGitRepositoryFrom(dir: File, gitProperties: GitProperties): GitRepository {
    val git = Git.init().setDirectory(dir).call()
    git.add().addFilepattern(".").call()
    git.commit()
        .setAuthor("zachtronics-bot-test", "zachtronics-bot-test@faendir.com")
        .setCommitter("zachtronics-bot-test", "zachtronics-bot-test@faendir.com")
        .setMessage("[BOT] initial commit")
        .call()
    return TestGitRepository(gitProperties, dir)
}