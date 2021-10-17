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
import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.discord.command.AbstractSubCommand;
import com.faendir.zachtronics.bot.discord.command.GameCommand;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.core.spec.MessageCreateFields;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.possible.Possible;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
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
public class SubCommandTest {

    @Autowired
    private ScCommand scCommand;

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

        Map<String, ? extends Serializable> args2 = Map.of("export", exportLink, "bypass-validation", true);
        String result = runCommand("archive", args2);
        assertTrue(result.contains("Passivation") && result.contains("`156/1/45/B`"));
    }

    @NotNull
    private static ApplicationCommandInteractionOption mockOption(String name, @NotNull Serializable value) {
        ApplicationCommandOption.Type type = value instanceof Boolean ? ApplicationCommandOption.Type.BOOLEAN :
                                                                        ApplicationCommandOption.Type.STRING;
        ApplicationCommandInteractionOptionValue optionValue = new ApplicationCommandInteractionOptionValue(
                Mockito.mock(GatewayDiscordClient.class), null, type.getValue(), value.toString());

        ApplicationCommandInteractionOption option = Mockito.mock(ApplicationCommandInteractionOption.class);
        Mockito.when(option.getName()).thenReturn(name);
        Mockito.when(option.getValue()).thenReturn(Optional.of(optionValue));
        return option;
    }

    @NotNull
    private <T> String runCommand(String commandName, @NotNull Map<String, ? extends Serializable> args) {
        ApplicationCommandInteractionOption subCommandOption = Mockito.mock(ApplicationCommandInteractionOption.class);
        Mockito.when(subCommandOption.getName()).thenReturn(commandName);
        Mockito.when(subCommandOption.getType()).thenReturn(ApplicationCommandOption.Type.SUB_COMMAND);

        List<ApplicationCommandInteractionOption> options = args.entrySet().stream()
                                                                .map(e -> mockOption(e.getKey(), e.getValue()))
                                                                .toList();
        Mockito.when(subCommandOption.getOptions()).thenReturn(options);

        ChatInputInteractionEvent interactionEvent = Mockito.mock(ChatInputInteractionEvent.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(interactionEvent.getOptions()).thenReturn(Collections.singletonList(subCommandOption));

        User ieee = new User(Mockito.mock(GatewayDiscordClient.class), Mockito.mock(UserData.class, Mockito.RETURNS_DEEP_STUBS));
        Mockito.when(ieee.getId().asLong()).thenReturn(295868901042946048L);
        Mockito.when(interactionEvent.getInteraction().getUser()).thenReturn(ieee);

        CombinedParseResult<GameCommand.SubCommandWithParameters<?>> parseResult = scCommand.parse(interactionEvent);
        if (!(parseResult instanceof CombinedParseResult.Success))
            return parseResult.toString();

        @SuppressWarnings("unchecked")
        GameCommand.SubCommandWithParameters<T> subCommandWithParameters = (GameCommand.SubCommandWithParameters<T>)(
                (CombinedParseResult.Success<GameCommand.SubCommandWithParameters<?>>) parseResult).getValue();
        AbstractSubCommand<T> subCommand = (AbstractSubCommand<T>) subCommandWithParameters.getSubCommand();
        InteractionReplyEditSpec editSpec = subCommand.handle(subCommandWithParameters.getParameters());

        String result = Stream.<Stream<String>>of(flatStream(editSpec.content()),
                                                  flatStream(editSpec.embeds()).flatMap(
                                                          l -> l.stream().mapMulti((e, d) -> {
                                                              e.title().toOptional().ifPresent(d);
                                                              e.description().toOptional().ifPresent(d);
                                                              e.fields().stream()
                                                               .map(f -> f.name() + "\n" + f.value())
                                                               .forEach(d);
                                                              EmbedCreateFields.Footer footer = e.footer();
                                                              if (footer != null)
                                                                  d.accept(footer.text());
                                                          })),
                                                  editSpec.files().stream().map(MessageCreateFields.File::name))
                              .flatMap(Function.identity()).collect(Collectors.joining("\n"));
        System.out.println(result);
        return result;
    }

    @NotNull
    private static <T> Stream<T> flatStream(@NotNull Possible<Optional<T>> p) {
        return Possible.flatOpt(p).stream();
    }

}