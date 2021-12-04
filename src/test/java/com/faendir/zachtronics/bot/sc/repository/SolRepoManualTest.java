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

package com.faendir.zachtronics.bot.sc.repository;

import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScSubmission;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

@BotTest
@Disabled("Massive tests only for manual testing or migrations")
class SolRepoManualTest {

    @Autowired
    private ScSolutionRepository repository;

    @Test
    public void testFullIO() {
        for (ScPuzzle p : ScPuzzle.values()) {
            Iterable<ScRecord> records = repository.findCategoryHolders(p, false).stream()
                                                   .map(CategoryRecord::getRecord)
                                                   .distinct()::iterator;
            for (ScRecord r : records) {
                ScSubmission submission = new ScSubmission(p, r.getScore(), r.getAuthor(), r.getDisplayLink(), "data");
                repository.submit(submission);
            }

            System.out.println("Done " + p.getDisplayName());
        }
        System.out.println("Done");
    }

    @Test
    public void migrameMucho() {
        for (ScPuzzle p : ScPuzzle.values()) {
            repository.findCategoryHolders(p, true).stream()
                      .map(cr -> cr.getRecord().getScore().toDisplayString() + " " +
                                 cr.getCategories().stream().map(ScCategory::name)
                                   .collect(Collectors.joining(",")))
                      .forEach(System.out::println);
        }
    }
}