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
import com.faendir.zachtronics.bot.discord.command.GameCommand;
import com.faendir.zachtronics.bot.discord.command.SubCommand;
import com.faendir.zachtronics.bot.model.StringFormat;
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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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
        assertTrue(result.contains("Pancakes"));
        assertEquals(2, StringUtils.countMatches(result, "](http"));
    }

    @Test
    public void testFrontier() {
        Map<String, String> args = Map.of("puzzle", "OPAS");
        String result = runCommand("frontier", args); // 9 frontier scores, 2 holding categories
        assertTrue(result.contains("Pancakes"));
        assertEquals(9, StringUtils.countMatches(result, "/1/"));
        assertEquals(2, StringUtils.countMatches(result, "]("));
        assertEquals(0, StringUtils.countMatches(result, "[\uD83D\uDCC4]"));

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
        SubCommand<T> subCommand = subCommandWithParameters.getSubCommand();

        Mockito.when(interactionEvent.editReply()).thenCallRealMethod();
        Mockito.when(interactionEvent.createFollowup()).thenCallRealMethod();
        var editSpecWrapper = new Object() {
            InteractionReplyEditSpec editSpec = null;
        };
        Mockito.when(interactionEvent.editReply(ArgumentMatchers.<InteractionReplyEditSpec>any())).then(invocation -> {
            editSpecWrapper.editSpec = invocation.getArgument(0, InteractionReplyEditSpec.class);
            return Mono.empty();
        });

        subCommand.handle(interactionEvent, subCommandWithParameters.getParameters()).block();

        var editSpec = editSpecWrapper.editSpec;
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
                              .flatMap(Function.identity())
                              .map(s -> s.replace(StringFormat.DISCORD.getSeparator(), "/"))
                              .collect(Collectors.joining("\n"));
        System.out.println(result);
        return result;
    }

    @NotNull
    private static <T> Stream<T> flatStream(@NotNull Possible<Optional<T>> p) {
        return Possible.flatOpt(p).stream();
    }

}