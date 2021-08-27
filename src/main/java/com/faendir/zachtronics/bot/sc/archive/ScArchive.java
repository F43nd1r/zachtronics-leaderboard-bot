package com.faendir.zachtronics.bot.sc.archive;

import com.faendir.zachtronics.bot.generic.archive.AbstractArchive;
import com.faendir.zachtronics.bot.generic.archive.SolutionsIndex;
import com.faendir.zachtronics.bot.main.git.GitRepository;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public Path relativePuzzlePath(@NotNull ScSolution solution) {
        return Paths.get(solution.getPuzzle().getGroup().name(), solution.getPuzzle().name());
    }

    @Override
    public SolutionsIndex<ScSolution> makeSolutionIndex(@NotNull Path puzzlePath,
                                                        @NotNull ScSolution solution) throws IOException {
        return new ScSolutionsIndex(puzzlePath);
    }
}
