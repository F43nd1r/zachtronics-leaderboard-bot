package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.Record;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class ScRecord implements Record {
    public static final ScRecord IMPOSSIBLE_CATEGORY = new ScRecord(ScScore.INVALID_SCORE, "", "", false);

    @NotNull ScScore score;
    @NotNull String author;
    @NotNull String link;
    boolean oldVideoRNG;
}