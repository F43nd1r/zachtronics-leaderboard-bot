package com.faendir.om.discord

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import javax.annotation.PostConstruct

@Service
class GitService(private val gitProperties: GitProperties) {
    private val repo = Files.createTempDirectory("om-leaderboard").toFile()

    @PostConstruct
    fun prepare() {
        Git.cloneRepository()
            .setURI("https://github.com/F43nd1r/om-leaderboard.git")
            .setDirectory(repo)
            .call()
    }

    fun update(puzzle: String, username: String, changeData: (File) -> Unit) {
        Git.open(repo).use {
            it.pull().call()
            changeData(repo)
            it.add().addFilepattern(".").call()
            if(!it.status().call().isClean) {
                it.commit().setAuthor("om-leaderboard-discord-bot", "om-leaderboard-discord-bot@faendir.com")
                    .setCommitter("om-leaderboard-discord-bot", "om-leaderboard-discord-bot@faendir.com")
                    .setMessage("Automated update with solution for $puzzle by $username")
                    .call()
                it.push().setCredentialsProvider(
                    UsernamePasswordCredentialsProvider(
                        gitProperties.username,
                        gitProperties.accessToken
                    )
                ).call()
            }
        }
    }
}