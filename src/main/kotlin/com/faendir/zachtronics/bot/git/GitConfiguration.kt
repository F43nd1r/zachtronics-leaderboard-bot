/*
 * Copyright (c) 2024
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
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class GitConfiguration(private val gitProperties: GitProperties) {

    @Bean("cwRepository")
    fun cwRepository() = GitRepository(gitProperties, "chipwizard-leaderboard", "https://github.com/lastcallbbs-community-developers/chipwizard-leaderboard.git")

    @Bean("fcRepository")
    fun fcRepository() = GitRepository(gitProperties, "foodcourt-leaderboard", "https://github.com/lastcallbbs-community-developers/foodcourt-leaderboard.git")

    @Bean("fpRepository")
    fun fpRepository() = GitRepository(gitProperties, "forbidden-path-leaderboard", "https://github.com/lastcallbbs-community-developers/forbidden-path-leaderboard.git")

    @Bean("ifRepository")
    fun ifRepository() = GitRepository(gitProperties, "infinifactory-leaderboard", "https://github.com/12345ieee/infinifactory-leaderboard.git")

    @Bean("omLeaderboardRepository")
    fun omLeaderboardRepository() = GitRepository(gitProperties, "om-leaderboard", "https://github.com/F43nd1r/om-leaderboard.git")

    @Bean("scArchiveRepository")
    fun scArchiveRepository() = GitRepository(gitProperties, "sc-archive", "https://github.com/spacechem-community-developers/spacechem-archive.git")

    @Bean("szRepository")
    fun szRepository() = GitRepository(gitProperties, "shenzhenIO-leaderboard", "https://github.com/12345ieee/shenzhenIO-leaderboard.git")
}