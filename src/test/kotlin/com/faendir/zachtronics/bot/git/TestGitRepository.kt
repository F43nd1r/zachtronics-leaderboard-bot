package com.faendir.zachtronics.bot.git

import com.faendir.zachtronics.bot.config.GitProperties
import java.io.File
import javax.annotation.PreDestroy

class TestGitRepository(gitProperties: GitProperties, private val directory: File) :
    GitRepository(gitProperties, directory.name, directory.toURI().toString()) {

    @PreDestroy
    fun cleanup2() {
        directory.deleteRecursively()
    }
}