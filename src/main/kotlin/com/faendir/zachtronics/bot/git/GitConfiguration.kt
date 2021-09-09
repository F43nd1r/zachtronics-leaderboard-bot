package com.faendir.zachtronics.bot.git

import com.faendir.zachtronics.bot.config.GitProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GitConfiguration(private val gitProperties: GitProperties) {

    @Bean("omGithubPagesLeaderboardRepository")
    fun omGithubPagesLeaderboardRepository() = GitRepository(gitProperties, "om-leaderboard", "https://github.com/F43nd1r/om-leaderboard.git", true)

    @Bean("omRedditLeaderboardRepository")
    fun omRedditLeaderboardRepository() = GitRepository(gitProperties, "om-wiki", "https://github.com/F43nd1r/OM-wiki.git")

    @Bean("omArchiveRepository")
    fun omArchiveRepository() = GitRepository(gitProperties, "om-archive", "https://github.com/F43nd1r/om-archive.git")

    @Bean("scArchiveRepository")
    fun scArchiveRepository() = GitRepository(gitProperties, "sc-archive", "https://github.com/spacechem-community-developers/spacechem-archive.git")

    @Bean("szRepository")
    fun szRepository() = GitRepository(gitProperties, "shenzhenIO-leaderboard", "https://github.com/12345ieee/shenzhenIO-leaderboard.git")

    @Bean("configRepository")
    fun configRepository() = GitRepository(gitProperties, "config", "https://github.com/F43nd1r/zachtronics-leaderboard-bot-config.git")
}