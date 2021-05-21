package com.faendir.zachtronics.bot.sz.discord;

import com.faendir.discord4j.command.annotation.OptionConverter;
import com.faendir.zachtronics.bot.sz.model.ShenzhenIO;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import org.jetbrains.annotations.Nullable;

public class SzPuzzleConverter implements OptionConverter<SzPuzzle> {
    @Override
    public SzPuzzle fromString(@Nullable String s) {
        if (s == null) throw new IllegalArgumentException();
        return ShenzhenIO.parsePuzzle(s);
    }
}
