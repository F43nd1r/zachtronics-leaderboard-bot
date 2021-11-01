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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@BotTest(Application.class)
public class SolRepoFindTest {

    @Autowired
    private ScSolutionRepository repository;

    @Test
    public void testGoodRecords() {
        ScRecord goodRecord = repository.find(ScPuzzle.research_example_1, ScCategory.C);
        System.out.println(goodRecord);
        goodRecord = repository.find(ScPuzzle.published_2_1, ScCategory.S);
        System.out.println(goodRecord);
        goodRecord = repository.find(ScPuzzle.sensing_6, ScCategory.RC);
        System.out.println(goodRecord);
        goodRecord = repository.find(ScPuzzle.production_tutorial_1, ScCategory.C);
        System.out.println(goodRecord);
        assertNotNull(goodRecord);
    }

    @Test
    public void testBadRecord() {
        ScRecord badRecord = repository.find(ScPuzzle.research_example_1, ScCategory.RC);
        assertNull(badRecord);
        badRecord = repository.find(ScPuzzle.research_example_1, ScCategory.CNP);
        assertNull(badRecord);
        badRecord = repository.find(ScPuzzle.bonding_7, ScCategory.RCNB);
        assertNull(badRecord);
        badRecord = repository.find(ScPuzzle.production_tutorial_1, ScCategory.RC);
        assertNull(badRecord);
    }

    @Test
    public void testFindCategoryHolders() {
        ScPuzzle puzzle = ScPuzzle.research_example_1;
        List<CategoryRecord<ScRecord, ScCategory>> categoryHolders = repository.findCategoryHolders(puzzle, false);
        assertEquals(2, categoryHolders.size());

        List<ScCategory> coveredCategories = categoryHolders.stream()
                                                            .map(CategoryRecord::getCategories)
                                                            .flatMap(Set::stream)
                                                            .sorted()
                                                            .toList();
        List<ScCategory> supportedCategories = Arrays.stream(ScCategory.values())
                                                     .filter(c -> c.supportsPuzzle(puzzle))
                                                     .sorted()
                                                     .toList();
        assertEquals(supportedCategories, coveredCategories);
    }

    @Test
    public void testFindParetoFrontier() {
        List<?> paretoFrontier = repository.findCategoryHolders(ScPuzzle.research_example_1, true);
        assertEquals(9, paretoFrontier.size());
    }
}
