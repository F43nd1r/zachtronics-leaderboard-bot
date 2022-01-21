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

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.sz.model.*;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@BotTest
@Disabled("Massive tests only for manual testing or migrations")
public class SzManualTest {

    @Autowired
    private SzSolutionRepository repository;

    @Test
    public void testFullIO() throws IOException {
        for (SzPuzzle p : SzPuzzle.values()) {

            Iterable<SzRecord> records = repository.findCategoryHolders(p, true).stream()
                                                   .map(CategoryRecord::getRecord)
                                                   ::iterator;
            for (SzRecord r : records) {
                String author = r.getAuthor() != null ? r.getAuthor() : "";
                SzSubmission submission = new SzSubmission(p, r.getScore(), author, Files.readString(r.getDataPath()));
                repository.submit(submission);
            }

            System.out.println("Done " + p.getDisplayName());
        }
        System.out.println("Done");
    }

    @Test
    public void bootstrapPsv() throws IOException {
        Path repoPath = Paths.get("../shenzhenIO/leaderboard");

        for (SzPuzzle puzzle : SzPuzzle.values()) {
            if (puzzle.getType() != SzType.STANDARD)
                continue;
            Path indexPath = repoPath.resolve(puzzle.getGroup().getRepoFolder()).resolve(puzzle.getId()).resolve("solutions.psv");
            try (ICSVWriter writer = new CSVWriterBuilder(Files.newBufferedWriter(indexPath)).withSeparator('|').build()) {

                Map<SzScore, CategoryRecord<SzRecord, SzCategory>> scoreMap =
                        repository.findCategoryHolders(puzzle, true).stream()
                                  .collect(Collectors.toMap(cr -> cr.getRecord().getScore(),
                                                            Function.identity(),
                                                            (cr1, cr2) -> {
                                                                cr1.getCategories().addAll(cr2.getCategories());
                                                                return cr1;
                                                            },
                                                            () -> new TreeMap<>(SzCategory.CP.getScoreComparator())));

                for (CategoryRecord<SzRecord, SzCategory> cr : scoreMap.values()) {
                    SzRecord record = cr.getRecord();
                    String author = record.getAuthor();
                    String categories = cr.getCategories().stream()
                                          .map(SzCategory::name)
                                          .sorted()
                                          .collect(Collectors.joining(","));

                    String[] csvRecord = new String[]{record.getScore().toDisplayString(),
                                                      author,
                                                      categories};
                    writer.writeNext(csvRecord, false);
                }
            }
        }
    }

    @Test
    public void renameSolutionFiles() throws IOException {
        Path repoPath = Paths.get("../shenzhenIO/leaderboard");

        for (SzPuzzle puzzle : SzPuzzle.values()) {
            if (puzzle.getType() != SzType.STANDARD)
                continue;
            Path puzzlePath = repoPath.resolve(puzzle.getGroup().getRepoFolder()).resolve(puzzle.getId());
            Files.list(puzzlePath)
                 .filter(p -> p.getFileName().toString().endsWith(".txt"))
                 .forEach(p -> {
                     try {
                         String data = Files.readString(p);
                         String filename = puzzle.getId() + "-" +
                                           SzSubmission.fromData(data, "").getScore().toDisplayString(DisplayContext.fileName()) + ".txt";
                         Path destPath = p.resolveSibling(filename);
                         if (!Files.exists(destPath))
                             Files.move(p, destPath);
                         else
                             Files.delete(p);
                     }
                     catch (IOException e) {
                         throw new UncheckedIOException(e);
                     }
                 });
        }
    }
}
