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

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.sc.model.*;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public void bootstrapPsv() throws IOException {
        Path repoPath = Paths.get("../spacechem/archive");

        for (ScPuzzle puzzle : ScPuzzle.values()) {
            Path indexPath = repoPath.resolve(puzzle.getGroup().name()).resolve(puzzle.name()).resolve("solutions.psv");
            try (ICSVWriter writer = new CSVWriterBuilder(Files.newBufferedWriter(indexPath)).withSeparator('|').build()) {

                for (CategoryRecord<ScRecord, ScCategory> cr : repository.findCategoryHolders(puzzle, true)) {
                    ScRecord record = cr.getRecord();
                    String author = record.getAuthor();
                    if (author == null) {
                        // pull from file, which must exist
                        assert record.getDataPath() != null;
                        author = ScSolutionMetadata.fromPath(record.getDataPath(), puzzle).getAuthor();
                    }
                    String categories = cr.getCategories().stream()
                                          .map(ScCategory::name)
                                          .collect(Collectors.joining(","));

                    String[] csvRecord = new String[]{record.getScore().toDisplayString(),
                                                      author,
                                                      record.getDisplayLink(),
                                                      record.isOldVideoRNG() ? "linux" : null,
                                                      categories};
                    writer.writeNext(csvRecord, false);
                }
            }
        }
    }
}