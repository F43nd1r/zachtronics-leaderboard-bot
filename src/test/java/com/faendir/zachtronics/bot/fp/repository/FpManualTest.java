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

package com.faendir.zachtronics.bot.fp.repository;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.fp.model.FpPuzzle;
import com.faendir.zachtronics.bot.fp.model.FpRecord;
import com.faendir.zachtronics.bot.fp.model.FpSubmission;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.utils.LambdaUtils;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@BotTest
@Disabled("Massive tests only for manual testing or migrations")
class FpManualTest {

    @Autowired
    private FpSolutionRepository repository;

    @Test
    public void testFullIO() {
        for (FpPuzzle p : FpPuzzle.values()) {
            List<ValidationResult<FpSubmission>> submissions =
                    repository.findCategoryHolders(p, true)
                              .stream()
                              .map(CategoryRecord::getRecord)
                              .map(FpManualTest::recordToSubmissions)
                              .<ValidationResult<FpSubmission>>map(ValidationResult.Valid::new)
                              .toList();

            repository.submitAll(submissions);

            System.out.println("Done " + p.getDisplayName());
        }
        System.out.println("Done");
    }

    @NotNull
    private static FpSubmission recordToSubmissions(@NotNull FpRecord record) {
        assert record.getDataPath() != null;
        String data = LambdaUtils.<Path, String>uncheckIOException(Files::readString).apply(record.getDataPath());
        return new FpSubmission(record.getPuzzle(), record.getScore(), record.getAuthor(),
                                record.getDisplayLink(), data);
    }

    @Test
    public void addLevelPrefixToSolutionFiles() throws IOException {
        Path repoPath = Paths.get("../bbs/forbidden-path-leaderboard");

        for (FpPuzzle puzzle : FpPuzzle.values()) {
            Path puzzlePath = repoPath.resolve(puzzle.getGroup().name()).resolve(puzzle.name());
            List<FpSolution> solutions = repository.unmarshalSolutions(puzzlePath);
            for (FpSolution solution : solutions) {
                Path path = puzzlePath.resolve(FpSolutionRepository.makeScoreFilename(solution.getScore()));

                String data = Files.readString(path);
                String newData = String.format("Toronto.Solution.%d.0 = %s", puzzle.getId(), data);
                Files.writeString(path, newData, StandardOpenOption.TRUNCATE_EXISTING);
            }
        }
    }
}