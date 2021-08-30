package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.generic.discord.Command;
import com.faendir.zachtronics.bot.sc.SpaceChemMarker;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.discordjson.json.WebhookExecuteRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.ApplicationCommandOptionType;
import discord4j.rest.util.MultipartRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
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
    @Disabled("Uses SChem")
    public void testSubmitArchive() {
        Map<String, String> args = Map.of("video", "http://example.com",
                                          "export", "https://pastebin.com/19smCuS8", // valid 45/1/14
                                          "author", "testMan");
        String result = runCommand("submit-archive", args);
        assertTrue(result.contains("Of Pancakes and Spaceships") && result.contains("45/1/14"));
    }

    @Test
    @Disabled("Uses SChem")
    public void testArchiveOne() {
        // we start at 100/100/100
        Map<String, String> args = Map.of("score", "45/1/14",
                                          "export", "https://pastebin.com/19smCuS8"); // valid 45/1/14
        String result = runCommand("archive", args);
        assertTrue(result.contains("Of Pancakes and Spaceships") && result.contains("`45/1/14`"));
    }

    @Test
    @Disabled("Uses SChem")
    public void testArchiveMany() {
        // we start at 100/100/100
        Map<String, String> args = Map.of("export", "https://pastebin.com/kNnfTvMa"); // valid 45/1/14 and 115/1/6
        String result = runCommand("archive", args);
        assertTrue(result.contains("Of Pancakes and Spaceships") && result.contains("`45/1/14`") &&
                   result.contains("`115/1/6`"));
    }

    @Test
    @Disabled("Uses SChem")
    public void testArchiveManyAndScore() {
        // we start at 1000/1/1000
        Map<String, String> args = Map.of("score", "136/1/27",
                                          "export", "https://pastebin.com/y7hG42XL"); // valid 136/1/27 and 236/1/11
        String result = runCommand("archive", args);
        assertTrue(result.contains("An Introduction to Sensing") && result.contains("`136/1/27`") &&
                   result.contains("`236/1/11/P`"));
    }

    @Test
    public void testArchiveBugged() {
        // we start at 100/100/100
        Map<String, String> args = Map.of("score", "50/1/50/B",
                                          "export", "https://pastebin.com/19smCuS8"); // valid 45/1/14
        String result = runCommand("archive", args);
        // we "trick" the archive
        assertTrue(result.contains("Of Pancakes and Spaceships") && result.contains("`45/1/14/B`"));
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
    private String runCommand(String commandName, @NotNull Map<String, String> args) {
        SlashCommandEvent slashCommandEvent = Mockito.mock(SlashCommandEvent.class, Mockito.RETURNS_DEEP_STUBS);

        List<ApplicationCommandInteractionOption> options = args.entrySet().stream()
                                                                .map(e -> mockOption(e.getKey(), e.getValue()))
                                                                .collect(Collectors.toList());
        Mockito.when(slashCommandEvent.getOptions()).thenReturn(options);

        MultipartRequest<WebhookExecuteRequest> multipartRequest = commands.stream().filter(c -> c.getData().name().equals(commandName))
                .findFirst().orElseThrow().handle(slashCommandEvent);
        WebhookExecuteRequest executeRequest = multipartRequest.getJsonPayload();
        assert executeRequest != null;
        String result = Stream.concat(stream(executeRequest.content()),
                                      stream(executeRequest.embeds()).flatMap(l -> l.stream().flatMap(
                                              e -> Stream.of(stream(e.title()),
                                                             stream(e.description()),
                                                             e.fields().toOptional().orElse(Collections.emptyList())
                                                              .stream().map(f -> f.name() + "\n" + f.value()))
                                                         .flatMap(Function.identity()))))
                              .collect(Collectors.joining("\n"));
        System.out.println(result);
        return result;
    }

    @NotNull
    private static <T> Stream<T> stream(@NotNull Possible<T> p) {
        return p.toOptional().stream();
    }
}