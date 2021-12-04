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

package com.faendir.zachtronics.bot.sz.repository;

import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.repository.SubmitResult;
import com.faendir.zachtronics.bot.sz.model.SzCategory;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzRecord;
import com.faendir.zachtronics.bot.sz.model.SzSubmission;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@BotTest
public class SzArchiveTest {

    @Autowired
    private SzSolutionRepository repository;

    @Test
    public void testArchive() {
        String content = """
                [name] Top solution Cost
                [puzzle] Sz000
                [production-cost] 600
                [power-usage] 57
                [lines-of-code] 8
                """;

        assertTrue(doArchive(content) instanceof SubmitResult.Success); // different content
        assertTrue(doArchive(content) instanceof SubmitResult.AlreadyPresent); // identical

        content = content.replace("[power-usage] 57", "[power-usage] 56");
        assertTrue(doArchive(content) instanceof SubmitResult.Success); // better

        content = content.replace("[power-usage] 56", "[power-usage] 100");
        assertTrue(doArchive(content) instanceof SubmitResult.NothingBeaten); // worse

        content = content.replace("[power-usage] 100", "nonsense");
        String finalContent = content;
        assertThrows(RuntimeException.class, () -> doArchive(finalContent)); // nonsensical
    }

    @Test
    public void testFrontier() {
        List<?> solutions = repository.findCategoryHolders(SzPuzzle.Sz000, true);
        assertEquals(4, solutions.size());
    }

    @NotNull
    private SubmitResult<SzRecord, SzCategory> doArchive(String content) {
        return repository.submit(new SzSubmission(content));
    }
}
