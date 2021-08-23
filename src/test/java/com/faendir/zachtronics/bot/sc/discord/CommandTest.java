package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.generic.discord.Command;
import com.faendir.zachtronics.bot.sc.SpaceChemMarker;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.Interaction;
import discord4j.discordjson.json.WebhookExecuteRequest;
import discord4j.rest.util.ApplicationCommandOptionType;
import discord4j.rest.util.MultipartRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@BotTest(SpaceChemMarker.SpaceChemConfiguration.class)
public class CommandTest {

    @Autowired
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
    public void testSubmit() {
        Map<String, String> args = Map.of("puzzle", "Tunnels I",
                                          "score", "1/1/1",
                                          "author", "testMan",
                                          "link", "http://example.com");
        String result = runCommand("submit", args);
        assertTrue(result.contains("Tunnels I") && result.contains("1/1/1"));
    }

    @Test
    public void testArchive() {
        // we start at 100/100/100
        Map<String, String> args = Map.of("puzzle", "Of Pancakes",
                                          "score", "50/50/50");
        String result = runCommand("archive", args);
        assertTrue(result.contains("`50/50/50` has been archived"));

        args = Map.of("puzzle", "Of Pancakes",
                      "link", "https://pastebin.com/19smCuS8"); // valid 45/1/14
        result = runCommand("archive", args);
        assertTrue(result.contains("`45/1/14/BP` has been archived"));

        args = Map.of("puzzle", "Of Pancakes",
                      "score", "45/1/14",
                      "link", "https://pastebin.com/19smCuS8"); // valid 45/1/14
        result = runCommand("archive", args);
        assertTrue(result.contains("`45/1/14` has been archived"));
    }

    @NotNull
    private static ApplicationCommandInteractionOption mockOption(String name, String value) {
        ApplicationCommandInteractionOptionValue optionValue = new ApplicationCommandInteractionOptionValue(
                Mockito.mock(GatewayDiscordClient.class), null, ApplicationCommandOptionType.STRING.getValue(), value);

        ApplicationCommandInteractionOption option = Mockito.mock(ApplicationCommandInteractionOption.class);
        Mockito.when(option.getName()).thenReturn(name);
        Mockito.when(option.getValue()).thenReturn(Optional.of(optionValue));
        return option;
    }

    @NotNull
    private String runCommand(String commandName, Map<String, String> args) {
        Interaction interaction = Mockito.mock(Interaction.class, Mockito.RETURNS_DEEP_STUBS);

        List<ApplicationCommandInteractionOption> options = args.entrySet().stream()
                                                                .map(e -> mockOption(e.getKey(), e.getValue()))
                                                                .collect(Collectors.toList());
        Mockito.when(interaction.getCommandInteraction().getOptions()).thenReturn(options);

        MultipartRequest<WebhookExecuteRequest> multipartRequest = commands.stream().filter(c -> c.getName().equals(commandName))
                .findFirst().orElseThrow().handle(interaction).block();
        assert multipartRequest != null;
        WebhookExecuteRequest executeRequest = multipartRequest.getJsonPayload();
        assert executeRequest != null;
        String result = Stream.concat(executeRequest.content().toOptional().stream(),
                                      executeRequest.embeds().toOptional().stream().flatMap(l -> l.stream().flatMap(
                                              e -> Stream.of(e.title().toOptional().stream(),
                                                             e.description().toOptional().stream(),
                                                             e.fields().toOptional().orElse(Collections.emptyList())
                                                              .stream().map(f -> f.name() + " " + f.value()))
                                                         .flatMap(Function.identity()))))
                              .collect(Collectors.joining("\n"));
        System.out.println(result);
        return result;
    }
}