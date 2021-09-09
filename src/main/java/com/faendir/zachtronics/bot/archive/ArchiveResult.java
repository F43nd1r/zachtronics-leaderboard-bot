package com.faendir.zachtronics.bot.archive;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Used to store archive result in the title string for games that don't use the title<br>
 * TODO: this is horrendous
 */
@Getter
@RequiredArgsConstructor
public enum ArchiveResult {
    FAILURE(""),
    ALREADY_ARCHIVED("ALREADY_ARCHIVED"),
    SUCCESS(".+");

    private final String titleString;
}
