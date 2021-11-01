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

package com.faendir.zachtronics.bot.sz.archive;

import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.archive.ArchiveResult;
import com.faendir.zachtronics.bot.model.Score;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzSolution;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@BotTest(Application.class)
public class SzArchiveTest {

    @Autowired
    private SzArchive archive;

    @Test
    public void testArchive() {
        String content = """
                [name] Top solution Cost
                [puzzle] Sz000
                [production-cost] 600
                [power-usage] 57
                [lines-of-code] 8
                """;

        assertTrue(doArchive(content) instanceof ArchiveResult.Success); // different content
        assertTrue(doArchive(content) instanceof ArchiveResult.AlreadyArchived); // identical

        content = content.replace("[power-usage] 57", "[power-usage] 56");
        assertTrue(doArchive(content) instanceof ArchiveResult.Success); // better

        content = content.replace("[power-usage] 56", "[power-usage] 100");
        assertTrue(doArchive(content) instanceof ArchiveResult.Failure); // worse
    }

    @Test
    public void testRetrieve() {
        Collection<Pair<Score, String>> solutions = archive.retrieve(SzPuzzle.Sz000);
        assertEquals(4, solutions.stream().filter(p -> p.getSecond() != null).count());
    }

    @NotNull
    private ArchiveResult doArchive(String content) {
        return archive.archive(new SzSolution(content));
    }
}
