package com.faendir.zachtronics.bot.model.sc;

import com.faendir.zachtronics.bot.model.Record;
import kotlinx.serialization.Serializable;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Serializable
@Value
public class ScRecord implements Record<ScScore> {
    public static final ScRecord IMPOSSIBLE_CATEGORY = new ScRecord(new ScScore(-1, -1, -1), "", "", false);

    ScScore score;
    String author;
    String link;
    boolean oldVideoRNG;

    @NotNull
    @Override
    public String toDisplayString() {
        return score.toDisplayString() + " by " + author + " " + link;
    }
}