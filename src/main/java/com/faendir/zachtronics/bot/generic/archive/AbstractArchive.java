package com.faendir.zachtronics.bot.generic.archive;

import com.faendir.zachtronics.bot.main.git.GitRepository;
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
        return getGitRepo().access(a -> performArchive(a, solution));
    }

    @NotNull
    @Override
    public List<Pair<String, String>> archiveAll(@NotNull Collection<? extends S> solution) {
        return getGitRepo().access(a -> solution.stream()
                                                .map(s -> performArchive(a, s))
                                                .collect(Collectors.toList()));
    }

    protected abstract Path relativePuzzlePath(@NotNull S solution);

    protected abstract SolutionsIndex<S> makeSolutionIndex(@NotNull Path puzzlePath,
                                                           @NotNull S solution) throws IOException;

    @NotNull
    private Pair<String, String> performArchive(@NotNull GitRepository.AccessScope accessScope, @NotNull S solution) {
        Path repoPath = accessScope.getRepo().toPath();
        Path puzzlePath = repoPath.resolve(relativePuzzlePath(solution));
        boolean frontierChanged;
        try {
            SolutionsIndex<S> index = makeSolutionIndex(puzzlePath, solution);
            frontierChanged = index.add(solution);
        } catch (IOException e) {
            // failures could happen after we dirtied the repo, so we call reset&clean on the puzzle dir
            accessScope.resetAndClean(puzzlePath.toFile());
            log.warn("Recoverable error during archive: ", e);
            return new Pair<>("", "");
        }

        if (frontierChanged && !accessScope.status().isClean()) {
            accessScope.addAll(puzzlePath.toFile());
            String repoUrl = accessScope.originUrl().replaceFirst(".git$", "");
            String rawFilesUrl = repoUrl.replace("github.com/", "raw.githubusercontent.com/") + "/master/";
            String result = Stream.concat(accessScope.status().getChanged().stream(),
                                          accessScope.status().getAdded().stream())
                                  .map(f -> "[" + f.replaceFirst(".+/", "") + "](" + rawFilesUrl + f + ")")
                                  .collect(Collectors.joining(", "));
            accessScope.commitAndPush(
                    "Added " + solution.getScore().toDisplayString() + " for " + solution.getPuzzle().getDisplayName());
            result += "\n[commit " + accessScope.currentHash().substring(0, 7) + "](" + repoUrl + "/commit/" +
                      accessScope.currentHash() + ")";
            return new Pair<>(" ", result);
        }
        else
            return new Pair<>("", "");
    }
}
