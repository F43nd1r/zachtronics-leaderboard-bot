/*
 * Copyright (c) 2025
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

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.repository.SubmitResult;
import com.faendir.zachtronics.bot.sz.model.SzCategory;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzRecord;
import com.faendir.zachtronics.bot.sz.model.SzSubmission;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@BotTest
public class SzSolutionRepositoryTest {

    @Autowired
    private SzSolutionRepository repository;

    @Test
    public void testList() {
        List<?> solutions = repository.findCategoryHolders(SzPuzzle.Sz000, false);
        assertEquals(3, solutions.size());
    }

    @Test
    public void testFrontier() {
        List<?> solutions = repository.findCategoryHolders(SzPuzzle.Sz000, true);
        assertEquals(4, solutions.size());
    }

    @Test
    public void testSubmit() throws IOException {
        ClassPathResource resource = new ClassPathResource(
                "repositories/sz-leaderboard/FIRST_CAMPAIGN/fake-surveillance-camera/fake-surveillance-camera-6-57-8.txt");
        String content = Files.readString(resource.getFile().toPath());

        assertInstanceOf(SubmitResult.AlreadyPresent.class, doSubmit(content)); // identical score

        content = content.replace("[power-usage] 57", "[power-usage] 56");
        assertInstanceOf(SubmitResult.Success.class, doSubmit(content)); // better

        content = content.replace("[power-usage] 56", "[power-usage] 100");
        assertInstanceOf(SubmitResult.NothingBeaten.class, doSubmit(content)); // worse

        content = content.replace("[power-usage] 100", "nonsense");
        String finalContent = content;
        assertThrows(RuntimeException.class, () -> doSubmit(finalContent)); // nonsensical
    }

    @NotNull
    private SubmitResult<SzRecord, SzCategory> doSubmit(String content) {
        return repository.submit(SzSubmission.fromData(content, "someguy", null));
    }
}
