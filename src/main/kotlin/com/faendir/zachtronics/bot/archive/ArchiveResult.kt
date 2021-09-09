package com.faendir.zachtronics.bot.archive

sealed class ArchiveResult {
    class Success(val message: String) : ArchiveResult()

    class AlreadyArchived : ArchiveResult()

    class Failure : ArchiveResult()
}