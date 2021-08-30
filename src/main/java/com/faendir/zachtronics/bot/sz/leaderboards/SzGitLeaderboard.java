package com.faendir.zachtronics.bot.sz.leaderboards;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.leaderboards.Leaderboard;
import com.faendir.zachtronics.bot.sz.model.SzCategory;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzRecord;
import com.faendir.zachtronics.bot.sz.model.SzSolution;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SzGitLeaderboard implements Leaderboard<SzCategory, SzPuzzle, SzRecord> {
    @Getter
    private final List<SzCategory> supportedCategories = Arrays.asList(SzCategory.values());
    @Qualifier("szRepository")
    private final GitRepository gitRepository;

    private static final Pattern NAME_REGEX = Pattern
            .compile("top solution (?:cost|power|lines)(?:->(?:cost|power|lines))?(?: - (?<author>.+))?", Pattern.CASE_INSENSITIVE);

    @Nullable
    @Override
    public SzRecord get(@NotNull SzPuzzle puzzle, @NotNull SzCategory category) {
        return gitRepository.access(a -> readSolutionFile(findPuzzleFile(a, puzzle, category)));
    }

    @NotNull
    @Override
    public Map<SzCategory, SzRecord> getAll(@NotNull SzPuzzle puzzle, @NotNull Collection<? extends SzCategory> categories) {
        return gitRepository.access(
                a -> categories.stream().collect(Collectors.toMap(category -> category,
                                                                  category -> readSolutionFile(
                                                                          findPuzzleFile(a, puzzle, category)))));
    }

    @NotNull
    private static SzRecord readSolutionFile(Path solutionFile) {
        SzSolution solution = new SzSolution(solutionFile);
        Matcher m = NAME_REGEX.matcher(solution.getTitle());
        if (!m.matches())
            throw new IllegalStateException("Name does not match standard format: " + m.replaceFirst(""));
        String author = m.group("author");
        String link = "https://raw.githubusercontent.com/12345ieee/shenzhenIO-leaderboard/master/" +
                      solution.getPuzzle().getGroup().getRepoFolder() + "/" + solutionFile.getFileName();
        return new SzRecord(solution.getScore(), author, link);

    }

    private static Path findPuzzleFile(@NotNull GitRepository.AccessScope accessScope, @NotNull SzPuzzle puzzle,
                                       @NotNull SzCategory category) {
        Path repo = accessScope.getRepo().toPath();
        Path puzzleFolder = repo.resolve(puzzle.getGroup().getRepoFolder());
        Path puzzleFile = puzzleFolder.resolve(puzzle.getId() + "-" + category.getRepoSuffix() + ".txt");
        if (!Files.exists(puzzleFile)) {
            // we're missing the X02 subcategory, we just have a X01 file
            puzzleFile = puzzleFolder.resolve(puzzle.getId() + "-" + (category.getRepoSuffix() - 1) + ".txt");
        }
        return puzzleFile;
    }
}
