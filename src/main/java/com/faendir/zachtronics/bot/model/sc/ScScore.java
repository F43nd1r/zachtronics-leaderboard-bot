package com.faendir.zachtronics.bot.model.sc;

import com.faendir.zachtronics.bot.model.Score;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Value
public class ScScore implements Score {
    int cycles;
    int reactors;
    int symbols;
    @Accessors(fluent = true)
    boolean usesBugs;
    @Accessors(fluent = true)
    boolean usesPrecognition;

    /** ccc/r/ss[/BP] */
    @NotNull
    @Override
    public String toDisplayString() {
        String result = cycles + "/" + reactors + "/" + symbols;
        if (usesBugs || usesPrecognition) {
            result += "/";
            if (usesBugs) result += "B";
            if (usesPrecognition) result += "P";
        }
        return result;
    }
}
