package com.faendir.zachtronics.bot.sz.archive;

import com.faendir.zachtronics.bot.generic.archive.Archive;
import com.faendir.zachtronics.bot.main.git.GitRepository;
import com.faendir.zachtronics.bot.sz.model.SzSolution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SzArchive implements Archive<SzSolution> {
    @Qualifier("szRepository")
    private final GitRepository gitRepo;

    @NotNull
    @Override
    public Mono<List<String>> archive(@NotNull SzSolution solution) {
        return gitRepo.access(a -> performArchive(a, solution));
    }

    @NotNull
    private List<String> performArchive(@NotNull GitRepository.AccessScope accessScope, @NotNull SzSolution solution) {
        Path repoPath = accessScope.getRepo().toPath();
        Path folderPath = repoPath.resolve(solution.getPuzzle().getGroup().getRepoFolder());
        List<String> archiveResult;
        try {
            SzSolutionsIndex index = new SzSolutionsIndex(folderPath, solution.getPuzzle());
            archiveResult = index.add(solution);
        } catch (IOException e) {
            // failures could happen after we dirtied the repo, so we call reset&clean on the puzzle dir
            accessScope.resetAndClean(folderPath.toFile());
            log.warn("Recoverable error during archive: ", e);
            return Collections.emptyList();
        }

        if (!archiveResult.isEmpty() && !accessScope.status().isClean()) {
            accessScope.add(folderPath.toFile());
            accessScope.commitAndPush(
                    "Added " + solution.getScore().toDisplayString() + " for " + solution.getPuzzle().getDisplayName());
        }
        return archiveResult;
    }
}
