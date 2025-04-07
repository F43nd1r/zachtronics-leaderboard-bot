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

package com.faendir.zachtronics.bot.fp.discord;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.testutils.RunCommandKt;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@BotTest
public class FpCommandTest {

    @Autowired
    private FpCommandGroup fpCommand;

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses XBPGHSim")
    public void testSubmitText() {
        Map<String, String> args = Map.of("solution", "Toronto.Solution.1.2 = eNp7zczAIMDAwMDIAAEgmvE/EDCgC6IwYGxkDooCkBHIgkwcDAjAyDAoOGB3QQUAl6EJHw==",
                                          "author", "testMan");
        String result = runCommand("submit", args);
        assertThat(result).contains("1-1", "5R/0C/8F/0W");
    }

    @NotNull
    private String runCommand(String commandName, @NotNull Map<String, ? extends Serializable> args) {
        return RunCommandKt.mockGameCommandRun(fpCommand, commandName, args);
    }

}