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

package com.faendir.zachtronics.bot.fc.discord;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.testutils.RunCommandKt;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@BotTest
public class FcCommandTest {

    @Autowired
    private FcCommandGroup fcCommand;

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses FoodCourtSim")
    public void testSubmitOne() {
        Map<String, String> args = Map.of("solution", "https://cdn.discordapp.com/attachments/993498489717805076/1020989449531817984/2twelve-1.solution",
                                          "author", "testMan");
        String result = runCommand("submit", args);
        assertTrue(result.contains("2Twelve") && result.contains("8T/65k/16S/4W"));
    }

    @NotNull
    private String runCommand(String commandName, @NotNull Map<String, ? extends Serializable> args) {
        return RunCommandKt.mockGameCommandRun(fcCommand, commandName, args);
    }

}