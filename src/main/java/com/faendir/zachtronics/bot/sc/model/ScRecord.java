package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.Record;
import kotlinx.serialization.Serializable;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class ScRecord implements Record {
    public static final ScRecord IMPOSSIBLE_CATEGORY = new ScRecord(ScScore.INVALID_SCORE, "", "", false);

    ScScore score;
    String author;
    String link;
    boolean oldVideoRNG;
}