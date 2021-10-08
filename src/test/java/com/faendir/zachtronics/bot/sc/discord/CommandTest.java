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

import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.discord.command.Command;
import com.faendir.zachtronics.bot.sc.ScQualifier;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.json.WebhookExecuteRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.MultipartRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.util.function.Tuple2;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@BotTest(Application.class)
public class CommandTest {

    @Autowired
    @ScQualifier
    private List<Command> commands;

    @Test
    public void testShow() {
        Map<String, String> args = Map.of("puzzle", "fission I",
                                          "category", "C");
        String result = runCommand("show", args);
        assertTrue(result.contains("Fission I") && result.contains("C"));
    }

    @Test
    public void testList() {
        Map<String, String> args = Map.of("puzzle", "OPAS");
        String result = runCommand("list", args);
        assertTrue(result.contains("Pancakes") && result.contains("C"));
    }

    @Test
    @Disabled("Not actually exposed to Discord")
    public void testSubmit() {
        Map<String, String> args = Map.of("puzzle", "Tunnels I",
                                          "score", "1/1/1",
                                          "author", "testMan",
                                          "video", "http://example.com");
        String result = runCommand("submit", args);
        assertTrue(result.contains("Tunnels I") && result.contains("1/1/1"));
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses SChem")
    public void testSubmitArchive() {
        Map<String, String> args = Map.of("video", "http://example.com",
                                          "export", "https://pastebin.com/19smCuS8", // valid 45/1/14
                                          "author", "testMan");
        String result = runCommand("submit-archive", args);
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
    @Disabled("Obscenely slow")
    public void testArchiveTooMany() {
        // we start at 100/100/100
        Map<String, String> args = Map.of("export", "https://pastebin.com/yEZKDh7T"); // 33x of GG, 23x archivable
        String result = runCommand("archive", args);
        assertTrue(result.contains("results hidden"));
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses SChem")
    public void testArchiveSudo() {
        String exportLink = "https://raw.githubusercontent.com/spacechem-community-developers/spacechem-archive/" +
                            "master/RESEARCHNET3/published_26_3/156-1-45-B.txt";
        Map<String, String> args1 = Map.of("export", exportLink);
        assertThrows(IllegalArgumentException.class, () -> runCommand("archive", args1));

        Map<String, String> args2 = Map.of("export", exportLink, "bypass-validation", "indeed");
        String result = runCommand("archive", args2);
        assertTrue(result.contains("Passivation") && result.contains("`156/1/45/B`"));
    }

    @NotNull
    private static ApplicationCommandInteractionOption mockOption(String name, String value) {
        ApplicationCommandInteractionOptionValue optionValue = new ApplicationCommandInteractionOptionValue(
                Mockito.mock(GatewayDiscordClient.class), null, ApplicationCommandOption.Type.STRING.getValue(), value);

        ApplicationCommandInteractionOption option = Mockito.mock(ApplicationCommandInteractionOption.class);
        Mockito.when(option.getName()).thenReturn(name);
        Mockito.when(option.getValue()).thenReturn(Optional.of(optionValue));
        return option;
    }

    @NotNull
    private String runCommand(String commandName, @NotNull Map<String, String> args) {
        ChatInputInteractionEvent ChatInputInteractionEvent = Mockito.mock(ChatInputInteractionEvent.class, Mockito.RETURNS_DEEP_STUBS);

        List<ApplicationCommandInteractionOption> options = args.entrySet().stream()
                                                                .map(e -> mockOption(e.getKey(), e.getValue()))
                                                                .collect(Collectors.toList());
        Mockito.when(ChatInputInteractionEvent.getOptions()).thenReturn(options);

        User ieee = new User(Mockito.mock(GatewayDiscordClient.class), Mockito.mock(UserData.class, Mockito.RETURNS_DEEP_STUBS));
        Mockito.when(ieee.getId().asLong()).thenReturn(295868901042946048L);
        Mockito.when(ChatInputInteractionEvent.getInteraction().getUser()).thenReturn(ieee);

        MultipartRequest<WebhookExecuteRequest> multipartRequest = commands.stream().filter(c -> c.getData().name()
                                                                                                  .equals(commandName))
                                                                           .findFirst().orElseThrow()
                                                                           .handle(ChatInputInteractionEvent);
        WebhookExecuteRequest executeRequest = multipartRequest.getJsonPayload();
        assert executeRequest != null;
        String result = Stream.<Stream<String>>of(stream(executeRequest.content()),
                                                  stream(executeRequest.embeds()).flatMap(
                                                          l -> l.stream().mapMulti((e, d) -> {
                                                              e.title().toOptional().ifPresent(d);
                                                              e.description().toOptional().ifPresent(d);
                                                              e.fields().toOptional().orElse(Collections.emptyList())
                                                               .stream().map(f -> f.name() + "\n" + f.value())
                                                               .forEach(d);
                                                              e.footer().toOptional()
                                                               .ifPresent(f -> d.accept(f.text()));
                                                          })), multipartRequest.getFiles().stream().map(Tuple2::getT1))
                              .flatMap(Function.identity()).collect(Collectors.joining("\n"));
        System.out.println(result);
        return result;
    }

    @NotNull
    private static <T> Stream<T> stream(@NotNull Possible<T> p) {
        return p.toOptional().stream();
    }
}