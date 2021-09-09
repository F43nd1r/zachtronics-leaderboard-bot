package com.faendir.zachtronics.bot.archive

/**
 * Used to store archive result in the title string for games that don't use the title<br></br>
 * TODO: this is horrendous
 */
enum class ArchiveResult(val titleString: String) {
    FAILURE(""),
    ALREADY_ARCHIVED("ALREADY_ARCHIVED"),
    SUCCESS(" ");
}