package com.faendir.zachtronics.bot.repository;

import com.faendir.zachtronics.bot.model.Category;
import com.faendir.zachtronics.bot.model.Puzzle;
import com.faendir.zachtronics.bot.model.Record;
import com.faendir.zachtronics.bot.model.Score;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.EnumSet;

public interface Solution<C extends Enum<C> & Category, P extends Puzzle<C>, S extends Score<C>, R extends Record<C>> {
    @NotNull S getScore();
    String getAuthor();
    @NotNull EnumSet<C> getCategories();

    CategoryRecord<R, C> extendToCategoryRecord(P puzzle, String dataLink, Path dataPath);

    @NotNull
    String[] marshal();
}
