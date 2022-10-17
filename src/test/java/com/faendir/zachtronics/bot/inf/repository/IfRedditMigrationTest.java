/*
 * Copyright (c) 2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.inf.repository;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.inf.model.IfPuzzle;
import com.faendir.zachtronics.bot.inf.model.IfScore;
import com.faendir.zachtronics.bot.inf.model.IfSubmission;
import com.faendir.zachtronics.bot.inf.model.IfType;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@BotTest
@Disabled("Massive tests only for manual migrations")
class IfRedditMigrationTest {

    @Autowired
    private IfSolutionRepository repository;

    @Test
    public void parseFootprintPage() throws IOException {
        Path pagePath = Paths.get("../infinifactory/footprint.md");
        List<String> lines = Files.readAllLines(pagePath);

        // cp -a ../infinifactory/leaderboard/* src/test/resources/repositories/if-leaderboard/

        Pattern scorePattern = Pattern.compile("(?<level>.+) \\| " +
                                               "(?<footprint>\\d+) \\| " +
                                               "[^|]+ \\|" +
                                               ".*?(?:Solution by (?<author>[^.()\\s]+)(?: .+|$))?", Pattern.CASE_INSENSITIVE);
        for (String line: lines) {
            Matcher m = scorePattern.matcher(line);
            if (m.matches()) {
                String levelName = m.group("level");
                IfPuzzle puzzle = findPuzzle(levelName);
                if (puzzle.getType() != IfType.STANDARD)
                    continue;

                int footprint = Integer.parseInt(m.group("footprint"));
                String author = m.group("author");
                if (author == null) author = "Community";
                IfScore score = new IfScore(9999, footprint, 9999, true, false);

                IfSubmission submission = new IfSubmission(puzzle, score, author, Collections.emptyList(), "");
                repository.submit(submission);
            }
        }

        // rsync -a --delete $(ls -1dt /tmp/if-leaderboard* | head -n1)/* ../infinifactory/leaderboard/
        // find . -size 0 -print -delete
        System.out.println("Done");
    }

    @Test
    public void parseBlocksPage() throws IOException {
        Path pagePath = Paths.get("../infinifactory/blocks.md");
        List<String> lines = Files.readAllLines(pagePath);

        // cp -a ../infinifactory/leaderboard/* src/test/resources/repositories/if-leaderboard/

        Pattern scorePattern = Pattern.compile("\\d+-\\d: (?<level>.+) \\| " +
                                               "(?<blocks>\\d+) \\| " +
                                               "\\[(?<author>[^]]+)\\]\\((?<link>[^)]+)\\)" +
                                               "(?: \\(\\[[^]]+\\]\\((?<link2>[^)]+)\\)\\))?" +
                                               "(?: \\(trivial\\))?" +
                                               "(?: \\| .+)?", Pattern.CASE_INSENSITIVE);
        for (String line: lines) {
            Matcher m = scorePattern.matcher(line);
            if (m.matches()) {
                String levelName = m.group("level");
                IfPuzzle puzzle = findPuzzle(levelName);
                if (puzzle.getType() != IfType.STANDARD)
                    continue;

                int blocks = Integer.parseInt(m.group("blocks"));
                String author = m.group("author");
                if (author.equals("###")) author = "Community";
                IfScore score = new IfScore(9999, 9999, blocks, true, false);

                List<String> displayLinks = new ArrayList<>(Collections.singleton(m.group("link")));
                if (m.group("link2") != null)
                    displayLinks.add(m.group("link2"));

                IfSubmission submission = new IfSubmission(puzzle, score, author, displayLinks, "");
                repository.submit(submission);
            }
        }

        // rsync -a --delete $(ls -1dt /tmp/if-leaderboard* | head -n1)/* ../infinifactory/leaderboard/
        // find . -size 0 -print -delete
        System.out.println("Done");
    }

    @Test
    public void parseCyclesPage() throws IOException {
        Path pagePath = Paths.get("../infinifactory/cycles.md");
        List<String> lines = Files.readAllLines(pagePath);

        // cp -a ../infinifactory/leaderboard/* src/test/resources/repositories/if-leaderboard/

        Pattern levelPattern = Pattern.compile("\\d+-\\d: (?<level>[^|]+?) +\\|.+");

        for (String line: lines) {
            if (line.equals("# Cheat Records"))
                break;
            Matcher m = levelPattern.matcher(line);
            if (m.matches()) {
                String levelName = m.group("level");
                IfPuzzle puzzle = findPuzzle(levelName);
                if (puzzle.getType() != IfType.STANDARD)
                    continue;

                String[] pieces = line.split(" *\\| *");
                if (pieces[3].equals("\"")) {
                    loadCylesSol(puzzle, pieces[1], pieces[2], false);
                }
                else {
                    loadCylesSol(puzzle, pieces[1], pieces[2], true);
                    loadCylesSol(puzzle, pieces[3], pieces[4], false);
                }
            }
        }

        // rsync -a --delete $(ls -1dt /tmp/if-leaderboard* | head -n1)/* ../infinifactory/leaderboard/
        // find . -size 0 -print -delete
        System.out.println("Done");
    }

    private static final Pattern LINKS_PATTERN = Pattern.compile("\\[(?<author>[^]]+)\\]\\((?<link1>[^)]+)\\)" +
                                                                 "(?: *\\[\\^\\[2\\]\\]\\((?<link2>[^)]+)\\))?" +
                                                                 "(?: *\\[\\^\\[3\\]\\]\\((?<link3>[^)]+)\\))?" +
                                                                 "(?: *\\[\\^\\[4\\]\\]\\((?<link4>[^)]+)\\))?");
    private void loadCylesSol(IfPuzzle puzzle, @NotNull String piece1, String piece2, boolean usesGRA) {
        Matcher ma = LINKS_PATTERN.matcher(piece2);
        if (piece1.matches("\\d+") && ma.matches()) {
            int cycles = Integer.parseInt(piece1);
            String author = ma.group("author");
            IfScore score = new IfScore(cycles, 9999, 9999, usesGRA, false);

            List<String> displayLinks = IntStream.rangeClosed(1, 4)
                                                 .mapToObj(i -> ma.group("link" + i))
                                                 .filter(Objects::nonNull)
                                                 .toList();

            IfSubmission submission = new IfSubmission(puzzle, score, author, displayLinks, "");
            repository.submit(submission);
        }
    }

    @Test
    public void parseAllPage() throws IOException {
        Path pagePath = Paths.get("../infinifactory/all.md");
        List<String> lines = Files.readAllLines(pagePath);

        // cp -a ../infinifactory/leaderboard/* src/test/resources/repositories/if-leaderboard/

        Pattern scorePattern = Pattern.compile("\\d+-\\d: (?<level>.+) \\| " +
                                               "\\[(?<cycles>\\d+)\\]\\((?<linkc>[^)]+)\\) \\| " +
                                               "\\[?(?<footprint>\\d+)(?:\\]\\((?<linkf>[^)]+)\\))? \\| " +
                                               "\\[(?<blocks>\\d+)\\]\\((?<linkb>[^)]+)\\)", Pattern.CASE_INSENSITIVE);
        for (String line: lines) {
            Matcher m = scorePattern.matcher(line);
            if (m.matches()) {
                String levelName = m.group("level");
                IfPuzzle puzzle = findPuzzle(levelName);
                if (puzzle.getType() != IfType.STANDARD)
                    continue;

                String author = "Community";

                int cycles = Integer.parseInt(m.group("cycles"));
                IfScore score = new IfScore(cycles, 9999, 9999, true, false);
                List<String> displayLinks = Collections.singletonList(m.group("linkc"));
                IfSubmission submission = new IfSubmission(puzzle, score, author, displayLinks, "");
                repository.submit(submission);

                int footprint = Integer.parseInt(m.group("footprint"));
                score = new IfScore(9999, footprint, 9999, true, false);
                displayLinks = m.group("linkf") == null ? Collections.emptyList() : Collections.singletonList(m.group("linkf"));
                submission = new IfSubmission(puzzle, score, author, displayLinks, "");
                repository.submit(submission);

                int blocks = Integer.parseInt(m.group("blocks"));
                score = new IfScore(9999, 9999, blocks, true, false);
                displayLinks = Collections.singletonList(m.group("linkb"));
                submission = new IfSubmission(puzzle, score, author, displayLinks, "");
                repository.submit(submission);
            }
        }

        // rsync -a --delete $(ls -1dt /tmp/if-leaderboard* | head -n1)/* ../infinifactory/leaderboard/
        // find . -size 0 -print -delete
        System.out.println("Done");
    }

    private static boolean terrestrialSurveySeen = false;
    private static IfPuzzle findPuzzle(@NotNull String levelName) {
        if (levelName.equals("Terrestrial Surveyor")) { // same name, 2 levels
            if (!terrestrialSurveySeen) {
                terrestrialSurveySeen = true;
                return IfPuzzle.LEVEL_5_4;
            }
            else {
                terrestrialSurveySeen = false;
                return IfPuzzle.LEVEL_7_5;
            }
        }

        List<IfPuzzle> candidates = UtilsKt.fuzzyMatch(Arrays.asList(IfPuzzle.values()), levelName, IfPuzzle::getDisplayName);
        if (candidates.size() == 1)
            return candidates.get(0);
        else
            throw new IllegalStateException("\"" + levelName + "\" bad");
    }
}