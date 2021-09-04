package com.faendir.zachtronics.bot.sc.archive;

import com.faendir.zachtronics.bot.archive.AbstractArchive;
import com.faendir.zachtronics.bot.archive.SolutionsIndex;
import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
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
public class ScArchive extends AbstractArchive<ScSolution> {
    @Getter
    @Qualifier("scArchiveRepository")
    private final GitRepository gitRepo;

    @Override
    protected Path relativePuzzlePath(@NotNull ScSolution solution) {
        return Paths.get(solution.getPuzzle().getGroup().name(), solution.getPuzzle().name());
    }

    @Override
    protected SolutionsIndex<ScSolution> makeSolutionIndex(@NotNull Path puzzlePath,
                                                           @NotNull ScSolution solution) throws IOException {
        return new ScSolutionsIndex(puzzlePath);
    }

    public String makeArchiveLink(@NotNull ScPuzzle puzzle, @NotNull ScScore score) {
        return String.format("%s/%s/%s/%s.txt", getGitRepo().getRawFilesUrl(), puzzle.getGroup().name(), puzzle.name(),
                             score.toDisplayString().replace('/', '-'));
    }

    public String makeArchiveLink(@NotNull ScSolution solution) {
        return makeArchiveLink(solution.getPuzzle(), solution.getScore());
    }
}
