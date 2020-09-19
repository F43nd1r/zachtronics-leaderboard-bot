package com.faendir.zachtronics.bot.model.sc;

import com.faendir.zachtronics.bot.model.Record;
import kotlinx.serialization.Serializable;
import lombok.Value;

@Serializable
@Value
public class ScRecord implements Record {
    ScCategory category;
    ScScore score;
    String link;
}