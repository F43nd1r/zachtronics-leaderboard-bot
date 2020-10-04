package com.faendir.zachtronics.bot.leaderboards.sz;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.leaderboards.Leaderboard;
import com.faendir.zachtronics.bot.model.sz.SzCategory;
import com.faendir.zachtronics.bot.model.sz.SzPuzzle;
import com.faendir.zachtronics.bot.model.sz.SzRecord;
import com.faendir.zachtronics.bot.model.sz.SzScore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class SzGitLeaderboard implements Leaderboard<SzCategory, SzScore, SzPuzzle, SzRecord> {
    @Getter
    private final List<SzCategory> supportedCategories = Arrays.asList(SzCategory.values());
    @Qualifier("szLeaderboardRepository")
    private final GitRepository gitRepository;

    private static final Pattern NAME_REGEX = Pattern
            .compile("\\[name] top solution [a-z]+(?:->[a-z]+)?(?: - (?<author>.+))?", Pattern.CASE_INSENSITIVE);
    @Nullable
    @Override
    public SzRecord get(@NotNull SzPuzzle puzzle, @NotNull SzCategory category) {
        Path solutionFile = gitRepository.access(a -> findPuzzleFile(a, puzzle, category));
        /*
        [name] Top solution Cost->Power - andersk
        [puzzle] Sz040
        [production-cost] 1200
        [power-usage] 679
        [lines-of-code] 31
         */
        try (Stream<String> solutionLines = Files.lines(solutionFile)) {
            Iterator<String> it = solutionLines.iterator();

            Matcher m = NAME_REGEX.matcher(it.next());
            if (!m.matches()) return null;
            String author = m.group("author");
            it.next(); // puzzle
            int cost = Integer.parseInt(it.next().replaceFirst("^.+] ", "")) / 100;
            int power = Integer.parseInt(it.next().replaceFirst("^.+] ", ""));
            int lines = Integer.parseInt(it.next().replaceFirst("^.+] ", ""));
            String link = "https://raw.githubusercontent.com/12345ieee/shenzhenIO-leaderboard/master/" +
                          puzzle.getGroup().getRepoFolder() + "/" + solutionFile.getFileName();
            return new SzRecord(new SzScore(cost, power, lines), author, link);
        }
        catch (IOException e) {
            return null;
        }
    }

    private static Path findPuzzleFile(GitRepository.AccessScope accessScope, SzPuzzle puzzle, SzCategory category) {
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
