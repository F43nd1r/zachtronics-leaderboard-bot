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

package com.faendir.zachtronics.bot.git

import com.faendir.zachtronics.bot.config.GitProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GitConfiguration(private val gitProperties: GitProperties) {

    @Bean("omGithubPagesLeaderboardRepository")
    fun omGithubPagesLeaderboardRepository() = GitRepository(gitProperties, "om-leaderboard", "https://github.com/F43nd1r/om-leaderboard.git")

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