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

package com.faendir.zachtronics.bot

import com.faendir.zachtronics.bot.config.GitProperties
import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.main.git.TestGitRepository
import com.faendir.zachtronics.bot.main.reddit.TestRedditService
import com.faendir.zachtronics.bot.om.imgur.ImgurService
import com.faendir.zachtronics.bot.om.imgur.TestImgurService
import com.faendir.zachtronics.bot.reddit.RedditService
import discord4j.core.GatewayDiscordClient
import io.mockk.every
import io.mockk.mockk
import org.eclipse.jgit.api.Git
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.util.ResourceUtils
import reactor.core.publisher.Flux
import java.io.File
import java.nio.file.Files

@TestConfiguration
class TestConfiguration(private val gitProperties: GitProperties) {

    @Bean("configRepository")
    fun configRepository(): GitRepository {
        return createTestGitRepository("repositories/config")
    }

    @Bean("omGithubPagesLeaderboardRepository")
    fun omGithubPagesLeaderboardRepository(): GitRepository {
        return createTestGitRepository("repositories/om-leaderboard")
    }

    @Bean("omRedditLeaderboardRepository")
    fun omRedditLeaderboardRepository(): GitRepository {
        return createTestGitRepository("repositories/om-wiki")
    }

    @Bean("scArchiveRepository")
    fun scArchiveRepository(): GitRepository {
        return createTestGitRepository("repositories/sc-archive")
    }

    @Bean("szRepository")
    fun szRepository(): GitRepository {
        return createTestGitRepository("repositories/sz-leaderboard")
    }

    @Primary
    @Bean
    fun redditService(): RedditService {
        return TestRedditService(extractResourceDirectory("reddit"))
    }

    @Primary
    @Bean
    fun imgurService(): ImgurService {
        return TestImgurService()
    }

    @Primary
    @Bean
    fun discordClient(): GatewayDiscordClient {
        val client = mockk<GatewayDiscordClient>()
        every { client.guilds } returns Flux.empty()
        return client
    }

    private fun createTestGitRepository(dir: String): GitRepository {
        val target = extractResourceDirectory(dir)
        val git = Git.init().setDirectory(target).call()
        git.add().addFilepattern(".").call()
        git.commit()
            .setAuthor("zachtronics-bot-test", "zachtronics-bot-test@faendir.com")
            .setCommitter("zachtronics-bot-test", "zachtronics-bot-test@faendir.com")
            .setMessage("[BOT] initial commit")
            .call()
        return TestGitRepository(gitProperties, target)
    }

    private fun extractResourceDirectory(dir: String): File {
        val resourceDir = ResourceUtils.getFile("classpath:$dir")
        val target = Files.createTempDirectory(resourceDir.name).toFile()
        resourceDir.copyRecursively(target)
        return target
    }
}