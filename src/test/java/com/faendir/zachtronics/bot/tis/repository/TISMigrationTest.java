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

package com.faendir.zachtronics.bot.tis.repository;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.tis.model.TISPuzzle;
import com.faendir.zachtronics.bot.tis.model.TISScore;
import com.faendir.zachtronics.bot.tis.model.TISSubmission;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@BotTest
@Disabled("Massive tests only for manual migrations")
class TISMigrationTest {

    @Autowired
    private TISSolutionRepository repository;

    @Test
    public void parsePage() throws IOException {
        Path pagePath = Paths.get("../tis100/wiki.md");
        List<String> lines = Files.readAllLines(pagePath);

        // cp -a ../tis100/leaderboard/* src/test/resources/repositories/tis-leaderboard/

        Pattern scorePattern = Pattern.compile("^(?<level>[A-Z\\d -]+)(?: \\((?<achievement>.+)\\))?\\|" +
                                               "(?:(?<author1>[^()|]+) - )?(?<score1>[\\d/c]+)?\\|" +
                                               "(?:(?<author2>[^()|]+) - )?(?<score2>[\\d/c]+)?\\|" +
                                               "(?:(?<author3>[^()|]+) - )?(?<score3>[\\d/c]+)?\\|" +
                                               "(?:(?<author4>[^()|]+) - )?(?<score4>[\\d/c]+)?$");
        for (String line: lines) {
            Matcher m = scorePattern.matcher(line);
            if (m.matches()) {
                String levelName = m.group("level");
                TISPuzzle puzzle = findPuzzle(levelName);

                for (int i =1; i<=4; i++) {
                    String author = m.group("author" + i);
                    if (author == null)
                        author = "Community";

                    String scoreStr = m.group("score" + i);
                    if (scoreStr == null)
                        continue;
                    TISScore score = TISScore.parseScore(scoreStr);
                    if (score == null)
                        throw new IllegalArgumentException(scoreStr);
                    if (m.group("achievement") != null)
                        score = score.withAchievement(true);

                    TISSubmission submission = new TISSubmission(puzzle, score, author, null, "");
                    repository.submit(submission);
                }

                System.out.println("Done " + puzzle.getDisplayName());
            }
        }

        // rsync -a --delete $(ls -1dt /tmp/tis-leaderboard* | head -n1)/* ../tis100/leaderboard/
        // find . -size 0 -print -delete
        System.out.println("Done");
    }

    @Value
    private static class UserFrontier {
        String name;
        List<List<Integer>> data;
    }

    /** data that powered the <a href="http://landonkryger.com/tis100/frontier/?id=0">frontier graphs</a> */
    @Test
    public void parseExport() throws IOException {
//        SELF-TEST DIAGNOSTIC,1,1
//        {name:'',data:[[83,8,8]]}
//        SIGNAL AMPLIFIER,5,2
//        {name:'',data:[[84,5,9],[160,4,6]]},{name:'GltyBystndr',data:[[102,4,24],[108,4,16],[122,4,10]]}
        @SuppressWarnings("deprecation")
        ObjectMapper objectMapper = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

        Path pagePath = Paths.get("../tis100/frontier.dat");
        List<String> lines = Files.readAllLines(pagePath);

        // cp -a ../tis100/leaderboard/* src/test/resources/repositories/tis-leaderboard/

        for (Iterator<String> iterator = lines.iterator(); iterator.hasNext(); ) {
            String puzzleLine = iterator.next();
            String jsonLine = iterator.next();

            String puzzleStr = puzzleLine.substring(0, puzzleLine.indexOf(","));
            boolean achievement = puzzleStr.contains("(");
            if (achievement)
                puzzleStr = puzzleLine.substring(0, puzzleLine.indexOf("(") - 1);
            TISPuzzle puzzle = findPuzzle(puzzleStr);

            String correctJson = "[" + jsonLine + "]";
            UserFrontier[] frontier = objectMapper.readValue(correctJson, UserFrontier[].class);

            for (UserFrontier uf : frontier) {
                String author = uf.getName();
                if (author.isEmpty())
                    author = "Community";

                for (List<Integer> intScore : uf.getData()) {
                    TISScore score;
                    if (achievement)
                        score = new TISScore(intScore.get(0), intScore.get(1), intScore.get(2), true, false);
                    else
                        score = new TISScore(intScore.get(0), intScore.get(1), intScore.get(2), false, false);

                    TISSubmission submission = new TISSubmission(puzzle, score, author, null, "");
                    repository.submit(submission);
                }
            }

            System.out.println("Done " + puzzle.getDisplayName());
        }

        // rsync -a --delete $(ls -1dt /tmp/tis-leaderboard* | head -n1)/* ../tis100/leaderboard/
        // find . -size 0 -print -delete
        System.out.println("Done");
    }

    private static TISPuzzle findPuzzle(@NotNull String levelName) {
        List<TISPuzzle> candidates = UtilsKt.fuzzyMatch(Arrays.asList(TISPuzzle.values()), levelName, TISPuzzle::getDisplayName);
        if (candidates.size() == 1)
            return candidates.get(0);
        else
            throw new IllegalStateException("\"" + levelName + "\" bad");
    }
}