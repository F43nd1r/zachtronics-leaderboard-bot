/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.exa.repository;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.exa.model.ExaPuzzle;
import com.faendir.zachtronics.bot.exa.model.ExaScore;
import com.faendir.zachtronics.bot.exa.model.ExaSubmission;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static com.faendir.zachtronics.bot.exa.model.ExaPuzzle.*;

@BotTest
@Disabled("Massive tests only for manual migrations")
class ExaMigrationTest {

    @Autowired
    private ExaSolutionRepository repository;

    @Test
    public void parseWiki() throws IOException {
        Path pagePath = Paths.get("../exapunks/wiki.md");
        List<String> lines = Files.readAllLines(pagePath);

        // cp -a ../exapunks/leaderboard/* src/test/resources/repositories/exa-leaderboard/

        Pattern scorePattern = Pattern.compile("^(?<level>[^|]+[^| ])?\\s*\\|\\s*" +
                                               "(?:\\[?(?<score1>\\d+/\\d+/\\d+/?c?)(?:\\]\\((?<link1>[^()]+)\\))?)?\\s*\\|\\s*" +
                                               "(?:\\[?(?<score2>\\d+/\\d+/\\d+/?c?)(?:\\]\\((?<link2>[^()]+)\\))?)?\\s*\\|\\s*" +
                                               "(?:\\[?(?<score3>\\d+/\\d+/\\d+/?c?)(?:\\]\\((?<link3>[^()]+)\\))?)?\\s*\\|?$");
        ExaPuzzle puzzle = null;
        for (String line : lines) {
            Matcher m = scorePattern.matcher(line);
            if (m.matches()) {
                String levelName = m.group("level");
                if (levelName != null)
                    puzzle = findPuzzle(levelName);
                if (puzzle == null)
                    throw new IllegalStateException("Where puzzle?");

                for (int i = 1; i <= 3; i++) {
                    String scoreStr = m.group("score" + i);
                    if (scoreStr == null)
                        continue;
                    ExaScore score = ExaScore.parseScore(scoreStr);
                    if (score == null)
                        throw new IllegalArgumentException(scoreStr);

                    String link = m.group("link" + i);
                    ExaSubmission submission = new ExaSubmission(puzzle, score, "wikiBoy", link, new byte[0]);
                    repository.submit(submission);
                }

                System.out.println("Done " + puzzle.getDisplayName());
            }
        }

        // rsync -a --delete $(ls -1dt /tmp/exa-leaderboard* | head -n1)/* ../exapunks/leaderboard/
        // find . -size 0 -print -delete
        System.out.println("Done");
    }

    @Test
    public void parseRepo() throws IOException {
        Path repoPath = Paths.get("../exapunks/ExapunksRecords");

        // cp -a ../exapunks/leaderboard/* src/test/resources/repositories/exa-leaderboard/

        for (ExaPuzzle puzzle: repository.getTrackedPuzzles()) {
            Path puzzlePath = repoPath.resolve(repoNames.get(puzzle));
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(puzzlePath)) {
                for (Path path : paths) {
                    String scoreStr = path.getFileName().toString().replace("|old", "").replace('|', '/');
                    ExaScore score = ExaScore.parseScore(scoreStr);
                    if (score == null)
                        throw new IllegalArgumentException(scoreStr);
                    String link = "https://github.com/ExapunksBacardi/ExapunksRecords/tree/master/" +
                                  repoPath.relativize(path).toString().replace("|", "%7C");
                    ExaSubmission submission = new ExaSubmission(puzzle, score, "repoBoy", link, new byte[0]);
                    repository.submit(submission);
                }
            }
            System.out.println("Done " + puzzle.getDisplayName());
        }

        // rsync -a --delete $(ls -1dt /tmp/exa-leaderboard* | head -n1)/* ../exapunks/leaderboard/
        // find . -size 0 -print -delete
        System.out.println("Done");
    }

    @Test
    public void parseScrape() throws IOException {
        Path scrapePath = Paths.get("../exapunks/scrape.psv");

        // cp -a ../exapunks/leaderboard/* src/test/resources/repositories/exa-leaderboard/

        List<ExaSubmission> submissions;
        try (BufferedReader reader = Files.newBufferedReader(scrapePath)) {

            CSVParser parser = new CSVParserBuilder().withSeparator('|').build();
            CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();
            submissions = StreamSupport.stream(csvReader.spliterator(), false)
                                       .map(ExaMigrationTest::submissionFromScrape)
                                       .toList();
        }
        for (ExaSubmission submission : submissions) {
            repository.submit(submission);
        }

        // rsync -a --delete $(ls -1dt /tmp/exa-leaderboard* | head -n1)/* ../exapunks/leaderboard/
        // find . -size 0 -print -delete
        System.out.println("Done");
    }

    private static @NotNull ExaSubmission submissionFromScrape(String @NotNull [] fields) {
        // PB050|887/106/4|mplain|https://i.imgur.com/bEOwU81.gifv|AS,cAS
        assert fields.length == 5;

        ExaPuzzle puzzle = ExaPuzzle.valueOf(fields[0]);
        ExaScore score = ExaScore.parseScore(fields[1]);
        String author = fields[2];
        String link = fields[3];

        assert score != null;
        return new ExaSubmission(puzzle, score, author, link, new byte[0]);
    }

    private static final Map<ExaPuzzle, String> repoNames = Map.ofEntries(
        Map.entry(PB000, "TWN1"),
        Map.entry(PB001, "TWN2"),
        Map.entry(PB037, "TWN3"),
        Map.entry(PB002, "TWN4"),
        Map.entry(PB003B, "PIZZA"),
        Map.entry(PB004, "M1"),
        Map.entry(PB005, "SNAXNET1"),
        Map.entry(PB006B, "ZEBROS"),
        Map.entry(PB007, "HIGHWAY"),
        Map.entry(PB008, "UN1"),
        Map.entry(PB009, "UCB"),
        Map.entry(PB010B, "WORKHOUSE"),
        Map.entry(PB012, "BANK1"),
        Map.entry(PB011B, "M2"),
        Map.entry(PB013C, "TWN5"),
        Map.entry(PB015, "REDSHIFT"),
        Map.entry(PB016, "LIBRARY"),
        Map.entry(PB040, "MODEM1"),
        Map.entry(PB018, "EMERSONS"),
        Map.entry(PB038, "M3"),
        Map.entry(PB020, "SAWAYAMA"),
        Map.entry(PB021, "APL"),
        Map.entry(PB023, "XLB"),
        Map.entry(PB024, "KRO"),
        Map.entry(PB028, "KGOG"),
        Map.entry(PB025, "BANK2"),
        Map.entry(PB026B, "MODEM2"),
        Map.entry(PB029B, "SNAXNET2"),
        Map.entry(PB030, "M4"),
        Map.entry(PB032, "HOLMAN"),
        Map.entry(PB033, "USGov"),
        Map.entry(PB034, "UN2"),
        Map.entry(PB035B, "MODEM3"),
        Map.entry(PB036, "M5"),

        Map.entry(PB054, "BLOODLUST"),
        Map.entry(PB053, "MVA"),
        Map.entry(PB050, "CYBERMYTH"),
        Map.entry(PB056, "USDoD"),
        Map.entry(PB051, "MODEM4"),
        Map.entry(PB057, "SCHOOL"),
        Map.entry(PB052, "x10x10x"),
        Map.entry(PB055, "AIRPLANES"),
        Map.entry(PB058, "MOSS"));

    private static ExaPuzzle findPuzzle(@NotNull String levelName) {
        List<ExaPuzzle> candidates = UtilsKt.fuzzyMatch(Arrays.asList(ExaPuzzle.values()), levelName, ExaPuzzle::getDisplayName);
        if (candidates.size() == 1)
            return candidates.get(0);
        else
            throw new IllegalStateException("\"" + levelName + "\" bad");
    }
}