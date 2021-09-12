/*
 * Copyright (c) 2021
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

package com.faendir.zachtronics.bot.sc.leaderboards;

import com.faendir.zachtronics.bot.leaderboards.Leaderboard;
import com.faendir.zachtronics.bot.leaderboards.UpdateResult;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.sc.archive.ScArchive;
import com.faendir.zachtronics.bot.sc.model.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.faendir.zachtronics.bot.sc.model.ScCategory.*;

@Component
@RequiredArgsConstructor
public class ScRedditLeaderboard implements Leaderboard<ScCategory, ScPuzzle, ScRecord> {
    private static final ScCategory[][] CATEGORIES = {{C,  CNB,  CNP},  {S,  SNB,  SNP},
                                                      {RC, RCNB, RCNP}, {RS, RSNB, RSNP}};
    @Getter
    private final List<ScCategory> supportedCategories = Arrays.asList(ScCategory.values());
    private final RedditService redditService;
    private final ScArchive archive;

    @Nullable
    @Override
    public ScRecord get(@NotNull ScPuzzle puzzle, @NotNull ScCategory category) {
        String[] lines = redditService.getWikiPage(Subreddit.SPACECHEM, puzzle.getGroup().getWikiPage()).split("\\r?\\n");

        boolean needsReactors = category.name().startsWith("R") && puzzle.getType() != ScType.PRODUCTION_TRIVIAL;
        String puzzleHeader = Pattern.quote(puzzle.getDisplayName()) + (needsReactors ? " - \\d Reactors?" : "");
        Pattern puzzleRegex = Pattern.compile("^\\s*\\|\\s*" + puzzleHeader);

        for (String line : lines) {
            if (!puzzleRegex.matcher(line).find())
                continue;

            String[] pieces = line.trim().split("\\s*\\|\\s*");
            List<String> tableCols = Arrays.asList(pieces).subList(2, pieces.length);
            List<String> halfTable = category.getDisplayName().contains("C") ? tableCols
                    .subList(0, tableCols.size() / 2) : tableCols.subList(tableCols.size() / 2, tableCols.size());
            ScRecord[] records = new ScRecord[3];
            parseHalfTable(puzzle, halfTable, records);

            int column = category.getDisplayName().endsWith("NP") ? 2 : category.getDisplayName()
                                                                                .endsWith("NB") ? 1 : 0;
            ScRecord record = records[column];
            if (record != ScRecord.IMPOSSIBLE_CATEGORY)
                return record;
            break;
        }
        return null;
    }

    @NotNull
    @Override
    public Map<ScCategory, ScRecord> getAll(@NotNull final ScPuzzle puzzle, @NotNull Collection<? extends ScCategory> categories) {
        String[] lines = redditService.getWikiPage(Subreddit.SPACECHEM, puzzle.getGroup().getWikiPage()).split("\\r?\\n");
        Pattern puzzleRegex = Pattern.compile("^\\s*\\|\\s*" + Pattern.quote(puzzle.getDisplayName()));
        Map<ScCategory, ScRecord> result = new EnumMap<>(ScCategory.class);
        int seenRows = 0;

        for (String line : lines) {
            if (puzzleRegex.matcher(line).find()) {
                String[] pieces = line.trim().split("\\s*\\|\\s*");
                List<String> tableCols = Arrays.asList(pieces).subList(2, pieces.length);
                ScRecord[] records = new ScRecord[3];

                List<String> cyclesHalf = tableCols.subList(0, tableCols.size() / 2);
                parseHalfTable(puzzle, cyclesHalf, records);
                for (int j = 0; j < 3; j++) {
                    if (records[j] != ScRecord.IMPOSSIBLE_CATEGORY) {
                        ScCategory category = CATEGORIES[2 * seenRows][j];
                        if (categories.contains(category))
                            result.put(category, records[j]);
                    }
                }

                List<String> symbolsHalf = tableCols.subList(tableCols.size() / 2, tableCols.size());
                parseHalfTable(puzzle, symbolsHalf, records);
                for (int j = 0; j < 3; j++) {
                    if (records[j] != ScRecord.IMPOSSIBLE_CATEGORY) {
                        ScCategory category = CATEGORIES[2 * seenRows + 1][j];
                        if (categories.contains(category))
                            result.put(category, records[j]);
                    }
                }

                seenRows++;
            }
            else if (seenRows != 0) {
                // we've already found the point and now we're past it, we're done
                break;
            }
        }
        return result;
    }

    @NotNull
    @Override
    public UpdateResult update(@NotNull ScPuzzle puzzle, @NotNull ScRecord record) {
        String[] lines = redditService.getWikiPage(Subreddit.SPACECHEM, puzzle.getGroup().getWikiPage()).split("\\r?\\n");
        Pattern puzzleRegex = Pattern.compile("^\\s*\\|\\s*" + Pattern.quote(puzzle.getDisplayName()));

        ScRecord[][] records = new ScRecord[4][3];
        int startingRow = -1;
        int seenRows = 0;
        int halfSize = -1;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (puzzleRegex.matcher(line).find()) {
                String[] pieces = line.trim().split("\\s*\\|\\s*");
                List<String> tableCols = Arrays.asList(pieces).subList(2, pieces.length);
                List<String> cyclesHalf = tableCols.subList(0, tableCols.size() / 2);
                parseHalfTable(puzzle, cyclesHalf, records[2 * seenRows]);
                List<String> symbolsHalf = tableCols.subList(tableCols.size() / 2, tableCols.size());
                parseHalfTable(puzzle, symbolsHalf, records[2 * seenRows + 1]);

                if (seenRows == 0) {
                    startingRow = i;
                    halfSize = tableCols.size() / 2;
                }
                seenRows++;
            }
            else if (seenRows != 0) {
                // we've already found the point and now we're past it, we're done
                break;
            }
        }

        Map<ScCategory, ScRecord> beatenRecords = new EnumMap<>(ScCategory.class);
        Map<ScCategory, ScRecord> relatedRecords = new EnumMap<>(ScCategory.class);
        // |Puzzle | [(**ccc**/r/ss) author](https://li.nk) | ← | [(ccc/r/**ss**) author](https://li.nk) | ←
        // |Puzzle - 1 Reactor | [(**ccc**/**r**/ss) author](https://li.nk) | ← | [(ccc/**r**/**ss**) author](https://li.nk) | ←
        for (int rowIdx = 0; rowIdx < seenRows; rowIdx++) {
            String[] prevElems = lines[startingRow + rowIdx].trim().split("\\s*\\|\\s*");
            StringBuilder row = new StringBuilder("|");
            int minReactors = Integer.MAX_VALUE;
            if (rowIdx == 1) {
                minReactors = Math.min(record.getScore().getReactors(), records[2][0].getScore().getReactors());
                row.append(puzzle.getDisplayName()).append(" - ").append(minReactors).append(" Reactor")
                   .append(minReactors == 1 ? "" : "s");
            }
            else {
                row.append(prevElems[1]);
            }

            for (int block = 0; block < 2; block++) {
                ScRecord[] blockRecords = records[2 * rowIdx + block];
                ScCategory[] blockCategories = CATEGORIES[2 * rowIdx + block];

                for (int i = 0; i < halfSize; i++) {
                    ScCategory thisCategory = blockCategories[i];
                    row.append(" | ");
                    if (thisCategory.supportsScore(record.getScore()) &&
                        thisCategory.getScoreComparator().compare(record.getScore(), blockRecords[i].getScore()) <= 0) {
                        beatenRecords.put(thisCategory, blockRecords[i]);
                        blockRecords[i] = record;

                        row.append(makeLeaderboardCell(blockRecords, i, minReactors, thisCategory));
                    }
                    else {
                        String prevElem = prevElems[block * halfSize + i + 2];
                        if (beatenRecords.containsValue(blockRecords[i]) && prevElem.matches("←+")) {
                            // "dangling" reference to beaten score, we need to write the actual score or change the pointer
                            row.append(makeLeaderboardCell(blockRecords, i, minReactors, thisCategory));
                        }
                        else {
                            if (blockRecords[i] != ScRecord.IMPOSSIBLE_CATEGORY &&
                                thisCategory.supportsScore(record.getScore()))
                                relatedRecords.put(thisCategory, blockRecords[i]);
                            row.append(prevElem);
                        }
                    }
                }
            }
            lines[startingRow + rowIdx] = row.toString();
        }

        if (!beatenRecords.isEmpty()) {
            redditService.updateWikiPage(Subreddit.SPACECHEM, puzzle.getGroup().getWikiPage(), String.join("\n", lines),
                                         puzzle.getDisplayName() + " " + record.getScore().toDisplayString() + " by " +
                                         record.getAuthor());
            return new UpdateResult.Success(beatenRecords);
        }
        else {
            return new UpdateResult.BetterExists(relatedRecords);
        }
    }

    @NotNull
    private static String makeLeaderboardCell(@NotNull ScRecord[] blockRecords, int i, int minReactors,
                                              ScCategory thisCategory) {
        ScRecord record = blockRecords[i];
        for (int prev = 0; prev < i; prev++) {
            if (record == blockRecords[prev]) {
                return "←".repeat(i - prev);
            }
        }

        String reactorPrefix = (record.getScore().getReactors() > minReactors) ? "† " : "";
        return record.toLbDisplayString(thisCategory.getFormatStringLb(), reactorPrefix);
    }

    private static final Pattern REGEX_SCORE_CELL =
            Pattern.compile("\\[\uD83D\uDCC4]\\(.+\\.txt\\) " +
                            "(?:† )?" +
                            "\\[\\((?<score>" + ScScore.REGEX_BP_SCORE + ")\\) (?<author>[^]]+)]" +
                            "\\((?<link>[^)]+)\\).*?");
    @NotNull
    private ScRecord parseLeaderboardRecord(ScPuzzle puzzle, String recordCell) {
        Matcher m = REGEX_SCORE_CELL.matcher(recordCell);
        if (m.matches()) {
            ScScore score = ScScore.parseBPScore(m);
            return new ScRecord(score, m.group("author"), m.group("link"),
                                archive.makeArchiveLink(puzzle, score),
                                m.group("oldRNG") != null);
        }
        throw new IllegalStateException("Leaderboard record unparseable: " + recordCell);
    }

    private void parseHalfTable(ScPuzzle puzzle, @NotNull List<String> halfTable, @NotNull ScRecord[] records) {
        records[0] = parseLeaderboardRecord(puzzle, halfTable.get(0));

        if (halfTable.get(1).startsWith("X"))
            records[1] = ScRecord.IMPOSSIBLE_CATEGORY;
        else if (halfTable.get(1).equals("←"))
            records[1] = records[0];
        else
            records[1] = parseLeaderboardRecord(puzzle, halfTable.get(1));

        if (halfTable.size() != 3 || halfTable.get(2).startsWith("X"))
            records[2] = ScRecord.IMPOSSIBLE_CATEGORY;
        else if (halfTable.get(2).equals("←←"))
            records[2] = records[0];
        else if (halfTable.get(2).equals("←"))
            records[2] = records[1];
        else
            records[2] = parseLeaderboardRecord(puzzle, halfTable.get(2));
    }
}
