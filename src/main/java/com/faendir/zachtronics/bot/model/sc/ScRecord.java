package com.faendir.zachtronics.bot.model.sc;

import com.faendir.zachtronics.bot.model.Record;
import kotlinx.serialization.Serializable;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Serializable
@Value
public class ScRecord implements Record<ScScore> {
    ScScore score;
    String author;
    String link;

    @NotNull
    @Override
    public String toDisplayString() {
        return score.toDisplayString() + " by " + author + " " + link;
    }
}