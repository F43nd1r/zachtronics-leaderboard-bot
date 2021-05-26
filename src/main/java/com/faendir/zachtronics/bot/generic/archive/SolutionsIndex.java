package com.faendir.zachtronics.bot.generic.archive;

import com.faendir.zachtronics.bot.model.Solution;

import java.io.IOException;
import java.util.List;

/** Interface for classes that handle archival data for a level in the respective repository */
public interface SolutionsIndex<T extends Solution> {

    /** @return stuff to write in the archival message, like displaced scores or won categories */
    List<String> add(T solution) throws IOException;
}
