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
        synchronized(repo) {
            Git.cloneRepository()
                .setURI("https://github.com/F43nd1r/om-leaderboard.git")
                .setDirectory(repo)
                .call()
        }
    }

    fun update(puzzle: String, username: String, changeData: (File) -> Unit) {
        synchronized(repo) {
            Git.open(repo).use { git ->
                git.pull().call()
                changeData(repo)
                git.add().addFilepattern(".").call()
                val missingStatus = git.status().call().missing
                missingStatus.takeIf { it.isNotEmpty() }?.fold(git.rm()) { rm, missing -> rm.addFilepattern(missing) }?.call()
                if (git.status().call().run { added.isNotEmpty() }) {
                    git.commit().setAuthor("om-leaderboard-discord-bot", "om-leaderboard-discord-bot@faendir.com")
                        .setCommitter("om-leaderboard-discord-bot", "om-leaderboard-discord-bot@faendir.com")
                        .setMessage("Automated update with solution for $puzzle by $username")
                        .call()
                    git.push().setCredentialsProvider(
                        UsernamePasswordCredentialsProvider(
                            gitProperties.username,
                            gitProperties.accessToken
                        )
                    ).call()
                }
            }
        }
    }

    fun read(readData: (File) -> Unit) {
        synchronized(repo) {
            Git.open(repo).use { git ->
                git.pull().call()
                readData(repo)
            }
        }
    }
}