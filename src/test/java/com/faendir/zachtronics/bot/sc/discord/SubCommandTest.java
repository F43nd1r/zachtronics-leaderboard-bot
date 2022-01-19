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

package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.parse.CombinedParseResult;
import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.discord.command.GameCommand;
import com.faendir.zachtronics.bot.discord.command.SubCommand;
import com.faendir.zachtronics.bot.discord.command.security.DiscordUser;
import com.faendir.zachtronics.bot.model.StringFormat;
import com.faendir.zachtronics.bot.testutils.RunCommandKt;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.core.spec.MessageCreateFields;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.possible.Possible;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@BotTest
public class SubCommandTest {

    @Autowired
    private ScCommand scCommand;

    @Test
    public void testShow() {
        Map<String, String> args = Map.of("puzzle", "fission I",
                                          "category", "C");
        String result = runCommand("show", args);
        assertTrue(result.contains("Fission I") && result.contains("C") && result.contains("]("));
    }

    @Test
    public void testList() {
        Map<String, String> args = Map.of("puzzle", "OPAS");
        String result = runCommand("list", args);
        assertTrue(result.contains("Pancakes"));
        assertEquals(2, StringUtils.countMatches(result, "](http"));
    }

    @Test
    public void testFrontier() {
        Map<String, String> args = Map.of("puzzle", "OPAS");
        String result = runCommand("frontier", args); // 9 frontier scores, 2 holding categories
        assertTrue(result.contains("Pancakes"));
        assertEquals(9, StringUtils.countMatches(result, "[\uD83D\uDCC4]"));
        assertEquals(9, StringUtils.countMatches(result, "/1/"));
        assertEquals(2, StringUtils.countMatches(result, "http"));

        args = Map.of("puzzle", "Fission I");
        result = runCommand("frontier", args); // 2 valid files
        assertTrue(result.contains("Fission I"));
        assertEquals(2, StringUtils.countMatches(result, "[\uD83D\uDCC4](file:/"));

        args = Map.of("puzzle", "Dessication Station");
        result = runCommand("frontier", args); // all 3 C categories
        assertTrue(result.contains("C\n") && result.contains("CNB\n") && result.contains("CNP\n"));
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses SChem")
    public void testSubmit() {
        Map<String, String> args = Map.of("video", "http://example.com",
                                          "export", "https://pastebin.com/19smCuS8", // valid 45/1/14
                                          "author", "testMan");
        String result = runCommand("submit", args);
        assertTrue(result.contains("Of Pancakes and Spaceships") && result.contains("45/1/14"));
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses SChem")
    public void testArchiveOne() {
        // we start at 100/100/100
        Map<String, String> args = Map.of("export", "https://pastebin.com/19smCuS8"); // valid 45/1/14
        String result = runCommand("archive", args);
        assertTrue(result.contains("Of Pancakes and Spaceships") && result.contains("`45/1/14`"));
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses SChem")
    public void testArchiveMany() {
        // we start at 100/100/100
        Map<String, String> args = Map.of("export", "https://pastebin.com/kNnfTvMa"); // valid 45/1/14 and 115/1/6
        String result = runCommand("archive", args);
        assertTrue(result.contains("Of Pancakes and Spaceships") && result.contains("`45/1/14`") &&
                   result.contains("`115/1/6`"));
    }

    @Test
    public void testArchiveTooMany() {
        Map<String, String> args = Map.of("export", "https://pastebin.com/Tf9nZ55Z"); // 60x OPAS headers
        assertThrows(IllegalArgumentException.class, () -> runCommand("archive", args));
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses SChem")
    public void testArchiveSudo() {
        String exportLink = "https://raw.githubusercontent.com/spacechem-community-developers/spacechem-archive/" +
                            "master/RESEARCHNET3/published_26_3/156-1-45-B.txt";
        Map<String, ? extends Serializable> args = Map.of("export", exportLink);
        String result = runCommand("archive", args);
        assertTrue(result.contains("156-1-45") && result.contains("Collision"));

        args = Map.of("export", exportLink, "bypass-validation", true);
        result = runCommand("archive", args);
        assertTrue(result.contains("Passivation") && result.contains("`156/1/45/B`"));
    }

    @NotNull
    private String runCommand(String commandName, @NotNull Map<String, ? extends Serializable> args) {
        return RunCommandKt.mockGameCommandRun(scCommand, commandName, args);
    }

}