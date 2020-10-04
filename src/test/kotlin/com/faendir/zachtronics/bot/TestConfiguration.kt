package com.faendir.zachtronics.bot

import com.faendir.zachtronics.bot.config.GitProperties
import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.git.TestGitRepository
import com.faendir.zachtronics.bot.imgur.ImgurService
import com.faendir.zachtronics.bot.imgur.TestImgurService
import com.faendir.zachtronics.bot.reddit.RedditService
import com.faendir.zachtronics.bot.reddit.TestRedditService
import net.dv8tion.jda.api.JDA
import org.eclipse.jgit.api.Git
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.util.ResourceUtils
import java.io.File
import java.nio.file.Files

@TestConfiguration
class TestConfiguration(private val gitProperties: GitProperties) {
    @Bean("omGithubPagesLeaderboardRepository")
    fun omGithubPagesLeaderboardRepository(): GitRepository {
        return createTestGitRepository("repositories/om-leaderboard")
    }

    @Bean("omRedditLeaderboardRepository")
    fun omRedditLeaderboardRepository(): GitRepository {
        return createTestGitRepository("repositories/om-wiki")
    }

    @Bean("configRepository")
    fun configRepository(): GitRepository {
        return createTestGitRepository("repositories/config")
    }

    @Bean("szLeaderboardRepository")
    fun szLeaderboardRepository(): GitRepository {
        return createTestGitRepository("repositories/shenzhenIO-leaderboard")
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

    @MockBean
    lateinit var jda : JDA

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