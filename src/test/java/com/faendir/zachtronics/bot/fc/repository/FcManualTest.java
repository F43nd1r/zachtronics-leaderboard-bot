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

package com.faendir.zachtronics.bot.fc.repository;


import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.fc.model.*;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.utils.LambdaUtils;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@BotTest
@Disabled("Massive tests only for manual testing or migrations")
class FcManualTest {

    @Autowired
    private FcSolutionRepository repository;

    @Test
    public void testFullIO() {
        for (FcPuzzle p : FcPuzzle.values()) {
            List<ValidationResult<FcSubmission>> submissions =
                    repository.findCategoryHolders(p, true)
                              .stream()
                              .map(CategoryRecord::getRecord)
                              .map(FcManualTest::recordToSubmissions)
                              .<ValidationResult<FcSubmission>>map(ValidationResult.Valid::new)
                              .toList();

            repository.submitAll(submissions);

            System.out.println("Done " + p.getDisplayName());
        }
        System.out.println("Done");
    }

    @NotNull
    private static FcSubmission recordToSubmissions(@NotNull FcRecord record) {
        assert record.getDataPath() != null;
        byte[] data = LambdaUtils.uncheckIOException(Files::readAllBytes).apply(record.getDataPath());
        return new FcSubmission(record.getPuzzle(), record.getScore(), record.getAuthor(),
                                record.getDisplayLink(), data);
    }

    @Test
    public void rebuildAllWiki() {
        for (FcPuzzle puzzle: FcPuzzle.values()) {
            if (puzzle.getType() != FcType.STANDARD)
                continue;
            repository.rebuildRedditLeaderboard(puzzle, "");
            System.out.println("Done " + puzzle.getDisplayName());
        }

        String page = repository.getRedditService().getWikiPage(repository.getSubreddit(), repository.getWikiPageName())
                                .replaceAll("file:/tmp/fc-leaderboard[0-9]+/",
                                            "https://raw.githubusercontent.com/lastcallbbs-community-developers/foodcourt-leaderboard/master");
        System.out.println(page);
    }

    @Test
    public void createWiki() {
        StringBuilder page = new StringBuilder();
        for (FcGroup group: FcGroup.values()) {
            String header = String.format("""
                                          ### %s

                                          | Name | Time | Cost | Sum of times | Wires
                                          | ---  | ---  | --- | --- | ---
                                          """, group.getDisplayName());
            page.append(header);
            String groupTable = Arrays.stream(FcPuzzle.values())
                                      .filter(p -> p.getGroup() == group)
                                      .map(p -> String.format("| [%s](%s) | | | | \n", p.getDisplayName(), p.getLink()))
                                      .collect(Collectors.joining("|\n"));
            page.append(groupTable).append('\n');
        }
        System.out.println(page);
    }
}