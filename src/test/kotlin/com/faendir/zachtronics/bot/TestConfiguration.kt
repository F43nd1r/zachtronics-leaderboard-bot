package com.faendir.zachtronics.bot

import com.faendir.zachtronics.bot.config.GitProperties
import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.reddit.RedditService
import com.faendir.zachtronics.bot.reddit.TestRedditService
import net.dv8tion.jda.api.JDA
import org.eclipse.jgit.api.Git
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.util.ResourceUtils
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

    @Primary
    @Bean
    fun redditService(): RedditService {
        return TestRedditService()
    }

    @MockBean
    lateinit var jda : JDA

    private fun createTestGitRepository(dir: String): GitRepository {
        val resourceDir = ResourceUtils.getFile("classpath:$dir")
        val target = Files.createTempDirectory(resourceDir.name).toFile()
        resourceDir.copyRecursively(target)
        val git = Git.init().setDirectory(target).call()
        git.add().addFilepattern(".").call()
        git.commit()
            .setAuthor("zachtronics-bot-test", "zachtronics-bot-test@faendir.com")
            .setCommitter("zachtronics-bot-test", "zachtronics-bot-test@faendir.com")
            .setMessage("[BOT] initial commit")
            .call()
        return GitRepository(gitProperties, target.name, target.toURI().toString())
    }
}