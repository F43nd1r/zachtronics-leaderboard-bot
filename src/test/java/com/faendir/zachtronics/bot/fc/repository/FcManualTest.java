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

package com.faendir.zachtronics.bot.fc.repository;


import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.TestConfigurationKt;
import com.faendir.zachtronics.bot.config.GitProperties;
import com.faendir.zachtronics.bot.fc.model.*;
import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.repository.SubmitResult;
import com.faendir.zachtronics.bot.utils.LambdaUtils;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@BotTest
@Disabled("Massive tests only for manual testing or migrations")
class FcManualTest {

    @Autowired
    private FcSolutionRepository repository;

    @TestConfiguration
    static class RepositoryConfiguration {
        @Bean("fcRepository")
        public static @NotNull GitRepository tisRepository(GitProperties gitProperties) {
            return TestConfigurationKt.readOnlyLocalClone("../bbs/foodcourt-leaderboard", gitProperties);
        }
    }

    @Test
    public void testFullIO() {
        for (FcPuzzle p : repository.getTrackedPuzzles()) {
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
        repository.rebuildRedditLeaderboard(null);
        String page = repository.getRedditService().getWikiPage(repository.getSubreddit(), repository.wikiPageName(null))
                                .replaceAll("file:[^()]+/foodcourt-leaderboard/",
                                            "https://raw.githubusercontent.com/lastcallbbs-community-developers/foodcourt-leaderboard/master");
        System.out.println(page);
    }

    @Test
    public void createWiki() {
        StringBuilder page = new StringBuilder();
        for (FcGroup group : FcGroup.values()) {
            String header = String.format("""
                                          ### %s
                                          
                                          | Name | Time | Cost | Sum of times | Wires
                                          | ---  | ---  | --- | --- | ---
                                          """, group.getDisplayName());
            page.append(header);
            String groupTable = repository.getTrackedPuzzles().stream()
                                          .filter(p -> p.getGroup() == group)
                                          .map(p -> String.format("| [%s](%s) | | | | \n", p.getDisplayName(), p.getLink()))
                                          .collect(Collectors.joining("|\n"));
            page.append(groupTable).append('\n');
        }
        System.out.println(page);
    }

    @Test
    public void submitSaveFolder() throws IOException {
        Path savesRoot = Paths.get("../bbs/saves");
//        List<String> authors = Files.list(savesRoot)
//                                    .filter(Files::isDirectory)
//                                    .map(p -> p.getFileName().toString())
//                                    .sorted(String.CASE_INSENSITIVE_ORDER)
//                                    .toList();
        List<String> authors = List.of("someGuy");

        for (String author : authors) {
            System.out.println("Starting " + author);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Files.list(savesRoot.resolve(author))
                 .filter(p -> p.getFileName().toString().endsWith(".solution"))
                 .forEach((p -> {
                     try {
                         baos.write(Files.readAllBytes(p));
                     }
                     catch (IOException e) {
                         throw new UncheckedIOException(e);
                     }
                 }));

            Collection<ValidationResult<FcSubmission>> submissions = FcSubmission.fromData(baos.toByteArray(), author);
            for (SubmitResult<FcRecord, FcCategory> result : repository.submitAll(submissions)) {
                System.out.println(result);
            }
        }

        /*
        rsync -a --delete --exclude=README.txt $(ls -1dt /tmp/foodcourt-leaderboard* | head -n1)/* ../tis100/leaderboard/
         */

        System.out.println("Done");
    }
}