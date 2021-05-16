package com.faendir.zachtronics.bot

import com.faendir.zachtronics.bot.main.config.GitProperties
import com.faendir.zachtronics.bot.main.git.GitRepository
import com.faendir.zachtronics.bot.main.git.TestGitRepository
import com.faendir.zachtronics.bot.main.reddit.RedditService
import com.faendir.zachtronics.bot.main.reddit.TestRedditService
import com.faendir.zachtronics.bot.om.imgur.ImgurService
import com.faendir.zachtronics.bot.om.imgur.TestImgurService
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