package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.OptionConverter;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.SpaceChem;
import org.jetbrains.annotations.Nullable;

public class ScPuzzleConverter implements OptionConverter<ScPuzzle> {
    @Override
    public ScPuzzle fromString(@Nullable String s) {
        if(s == null) throw new IllegalArgumentException();
        return SpaceChem.parsePuzzle(s);
    }
}
