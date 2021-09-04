package com.faendir.zachtronics.bot.sz.archive;

import com.faendir.zachtronics.bot.archive.AbstractArchive;
import com.faendir.zachtronics.bot.archive.SolutionsIndex;
import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzSolution;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class SzArchive extends AbstractArchive<SzSolution> {
    @Getter
    @Qualifier("szRepository")
    private final GitRepository gitRepo;

    @Override
    protected Path relativePuzzlePath(@NotNull SzSolution solution) {
        return Paths.get(solution.getPuzzle().getGroup().getRepoFolder());
    }

    @Override
    protected SolutionsIndex<SzSolution> makeSolutionIndex(@NotNull Path puzzlePath,
                                                           @NotNull SzSolution solution) throws IOException {
        return new SzSolutionsIndex(puzzlePath, solution.getPuzzle());
    }

    public String makeArchiveLink(@NotNull SzPuzzle puzzle, @NotNull String fileName) {
        return String.format("%s/%s/%s", getGitRepo().getRawFilesUrl(), puzzle.getGroup().getRepoFolder(), fileName);
    }
}
