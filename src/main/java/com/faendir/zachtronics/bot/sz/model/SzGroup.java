package com.faendir.zachtronics.bot.sz.model;

import com.faendir.zachtronics.bot.model.Group;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SzGroup implements Group {
    FIRST_CAMPAIGN("First Campaign", "first_campaign"),
    SECOND_CAMPAIGN("Second Campaign", "second_campaign"),
    BONUS_PUZZLES("Bonus Puzzles", "bonus_puzzles");

    private final String displayName;
    private final String repoFolder;
}
