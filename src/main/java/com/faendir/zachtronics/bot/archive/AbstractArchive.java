package com.faendir.zachtronics.bot.archive;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.model.Solution;
import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public abstract class AbstractArchive<S extends Solution> implements Archive<S> {
    protected abstract GitRepository getGitRepo();

    @NotNull
    @Override
    public Pair<String, String> archive(@NotNull S solution) {
        return getGitRepo().access(a -> {
            Pair<String, String> r = performArchive(a, solution);
            a.push();
            return r;
        });
    }

    @NotNull
    @Override
    public List<Pair<String, String>> archiveAll(@NotNull Collection<? extends S> solution) {
        return getGitRepo().access(a -> {
            List<Pair<String, String>> r = solution.stream()
                                                   .map(s -> performArchive(a, s))
                                                   .collect(Collectors.toList());
            a.push();
            return r;
        });
    }

    protected abstract Path relativePuzzlePath(@NotNull S solution);

    protected abstract SolutionsIndex<S> makeSolutionIndex(@NotNull Path puzzlePath,
                                                           @NotNull S solution) throws IOException;

    @NotNull
    private Pair<String, String> performArchive(@NotNull GitRepository.AccessScope accessScope, @NotNull S solution) {
        Path repoPath = accessScope.getRepo().toPath();
        Path puzzlePath = repoPath.resolve(relativePuzzlePath(solution));
        boolean newOrEqual;
        try {
            SolutionsIndex<S> index = makeSolutionIndex(puzzlePath, solution);
            newOrEqual = index.add(solution);
        } catch (IOException e) {
            // failures could happen after we dirtied the repo, so we call reset&clean on the puzzle dir
            accessScope.resetAndClean(puzzlePath.toFile());
            log.warn("Recoverable error during archive: ", e);
            return makeResult(ArchiveResult.FAILURE, "");
        }

        if (!newOrEqual) {
            return makeResult(ArchiveResult.FAILURE, "");
        }

        if (!accessScope.status().isClean()) {
            accessScope.addAll(puzzlePath.toFile());
            String result = Stream.concat(accessScope.status().getChanged().stream(),
                                          accessScope.status().getAdded().stream())
                                  .map(f -> "[" + f.replaceFirst(".+/", "") + "](" + getGitRepo().getRawFilesUrl() + f +
                                            ")").collect(Collectors.joining(", "));
            accessScope.commit(
                    "Added " + solution.getScore().toDisplayString() + " for " + solution.getPuzzle().getDisplayName());
            result += "\n[commit " + accessScope.currentHash().substring(0, 7) + "](" +
                      getGitRepo().getUrl().replaceFirst(".git$", "") + "/commit/" + accessScope.currentHash() + ")";
            return makeResult(ArchiveResult.SUCCESS, result);
        }
        else {
            // the same exact sol was already archived,
            return makeResult(ArchiveResult.ALREADY_ARCHIVED, "");
        }
    }

    @NotNull
    private static Pair<String, String> makeResult(@NotNull ArchiveResult archiveResult, String message) {
        return new Pair<>(archiveResult.getTitleString(), message);
    }
}
