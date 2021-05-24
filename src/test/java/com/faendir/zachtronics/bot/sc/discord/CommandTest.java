package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.generic.discord.Command;
import com.faendir.zachtronics.bot.sc.SpaceChemMarker;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.Interaction;
import discord4j.discordjson.json.WebhookExecuteRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.ApplicationCommandOptionType;
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
    public void testSubmit() {
        Map<String, String> args = Map.of("puzzle", "fission I",
                                          "score", "1/1/1",
                                          "author", "testMan",
                                          "link", "http://example.com");
        String result = runCommand("submit", args);
        assertTrue(result.contains("Fission I") && result.contains("1/1/1"));
    }

    /*@Test
    public void testArchive() {
        String text = "!archive Of Pancakes (10/10/10)";
        String attachment = null;
        String result = runCommand(text, attachment);
        assertTrue(result.contains("archived") && result.contains("archived 10/10/10"));

        text = "!archive Of Pancakes";
        attachment = "SOLUTION:name,author,8-8-8,title\nActual content...";
        result = runCommand(text, attachment);
        assertTrue(result.contains("archived") && result.contains("8/8/8/BP"));

        text = "!archive Of Pancakes (9/9/9)";
        attachment = "SOLUTION:name,author,9-9-9,title\nActual content...";
        result = runCommand(text, attachment);
        assertTrue(result.contains("archived") && result.contains("9/9/9"));
    }*/

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
        /*List<Attachment> attachments;
        if (attachmentContent != null) {
            Attachment attachment = Mockito.mock(Attachment.class);
            Mockito.when(attachment.retrieveInputStream()).thenReturn(
                    CompletableFuture.completedFuture(new ByteArrayInputStream(attachmentContent.getBytes())));
            attachments = Collections.singletonList(attachment);
        } else {
            attachments = Collections.emptyList();
        }*/

        Interaction interaction = Mockito.mock(Interaction.class, Mockito.RETURNS_DEEP_STUBS);

        List<ApplicationCommandInteractionOption> options = args.entrySet().stream()
                                                                .map(e -> mockOption(e.getKey(), e.getValue()))
                                                                .collect(Collectors.toList());
        Mockito.when(interaction.getCommandInteraction().getOptions()).thenReturn(options);

        WebhookExecuteRequest executeRequest = commands.stream().filter(c -> c.getName().equals(commandName))
                                                       .findFirst().orElseThrow().handle(interaction).block();
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