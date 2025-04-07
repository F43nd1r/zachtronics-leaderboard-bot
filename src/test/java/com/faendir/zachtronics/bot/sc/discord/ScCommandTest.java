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

package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.testutils.RunCommandKt;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@BotTest
public class ScCommandTest {

    @Autowired
    private ScCommandGroup scCommand;

    @Test
    public void testShow() {
        // Getting Pumped lacks a video for the C record but has it for the S record
        Map<String, String> args = Map.of("puzzle", "getting pumped",
                                          "category", "C");
        String result = runCommand("show", args);
        assertThat(result).contains("Getting Pumped", "C").doesNotContain("](http");

        args = Map.of("puzzle", "getting pumped",
                      "category", "S");
        result = runCommand("show", args);
        assertThat(result).contains("Getting Pumped", "S", "](http");
    }

    @Test
    public void testList() {
        Map<String, String> args = Map.of("puzzle", "OPAS");
        String result = runCommand("list", args);
        assertThat(result).contains("Pancakes");
        assertEquals(2, StringUtils.countMatches(result, "](http"));
    }

    @Test
    public void testFrontier() {
        Map<String, String> args = Map.of("puzzle", "OPAS");
        String result = runCommand("frontier", args); // 9 frontier scores, 2 holding categories
        assertThat(result).contains("Pancakes");
        assertEquals(9, StringUtils.countMatches(result, "[📄]"));
        assertEquals(9, StringUtils.countMatches(result, "/1/"));
        assertEquals(2, StringUtils.countMatches(result, "http"));

        args = Map.of("puzzle", "Get Pump");
        result = runCommand("frontier", args); // 2 valid solutions, 1 video-only
        assertThat(result).contains("Getting Pumped");
        assertEquals(2, StringUtils.countMatches(result, "[📄](file:/"));
        assertEquals(3, StringUtils.countMatches(result, "/1/"));
        assertEquals(2, StringUtils.countMatches(result, "http"));

        args = Map.of("puzzle", "Dessication Station");
        result = runCommand("frontier", args); // all 3 C categories
        assertThat(result).contains("C\n", "CNB\n", "CNP\n");
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses SChem")
    public void testSubmitVideo() {
        Map<String, String> args = Map.of("export", "https://pastebin.com/19smCuS8", // valid 45/1/14
                                          "author", "testMan",
                                          "video", "http://example.com");
        String result = runCommand("submit", args);
        assertThat(result).contains("Of Pancakes and Spaceships", "45/1/14");
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses SChem")
    public void testSubmitOne() {
        // we start at 100/100/100
        Map<String, String> args = Map.of("export", "https://pastebin.com/19smCuS8"); // valid 45/1/14
        String result = runCommand("submit", args);
        assertThat(result).contains("Of Pancakes and Spaceships", "`45/1/14`");
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses SChem")
    public void testSubmitMany() {
        // we start at 100/100/100
        Map<String, String> args = Map.of("export", "https://pastebin.com/kNnfTvMa"); // valid 45/1/14 and 115/1/6
        String result = runCommand("submit", args);
        assertThat(result).contains("Of Pancakes and Spaceships", "`45/1/14`", "`115/1/6`");
    }

    @Test
    public void testSubmitTooMany() {
        Map<String, String> args = Map.of("export", "https://pastebin.com/Tf9nZ55Z"); // 60x OPAS headers
        assertThrows(IllegalArgumentException.class, () -> runCommand("submit", args));
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses SChem")
    public void testSubmitSudo() {
        String exportLink = "https://raw.githubusercontent.com/spacechem-community-developers/spacechem-archive/" +
                            "master/RESEARCHNET3/published_26_3/156-1-45-B.txt";
        Map<String, ? extends Serializable> args1 = Map.of("export", exportLink);
        assertThatThrownBy(() -> runCommand("submit", args1))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("156-1-45", "Collision");

        Map<String, ? extends Serializable> args2 = Map.of("export", exportLink, "bypass-validation", true);
        String result = runCommand("submit", args2);
        assertThat(result).contains("Passivation", "`156/1/45/B`");
    }

    @NotNull
    private String runCommand(String commandName, @NotNull Map<String, ? extends Serializable> args) {
        return RunCommandKt.mockGameCommandRun(scCommand, commandName, args);
    }

}