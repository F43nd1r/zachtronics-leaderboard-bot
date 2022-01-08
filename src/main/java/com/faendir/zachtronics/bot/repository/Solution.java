package com.faendir.zachtronics.bot.repository;

import com.faendir.zachtronics.bot.model.Category;
import com.faendir.zachtronics.bot.model.Score;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Solution<S extends Score<C>, C extends Category> {
    @NotNull S getScore();
    String getAuthor();
    Set<C> getCategories();
}
