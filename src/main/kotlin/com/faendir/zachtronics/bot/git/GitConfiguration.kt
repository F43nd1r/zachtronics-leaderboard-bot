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
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class GitConfiguration(private val gitProperties: GitProperties) {

    @Bean("omLeaderboardRepository")
    fun omGithubPagesLeaderboardRepository() = GitRepository(gitProperties, "om-leaderboard", "https://github.com/F43nd1r/om-leaderboard.git")

    @Bean("scArchiveRepository")
    fun scArchiveRepository() = GitRepository(gitProperties, "sc-archive", "https://github.com/spacechem-community-developers/spacechem-archive.git")

    @Bean("szRepository")
    fun szRepository() = GitRepository(gitProperties, "shenzhenIO-leaderboard", "https://github.com/12345ieee/shenzhenIO-leaderboard.git")
}