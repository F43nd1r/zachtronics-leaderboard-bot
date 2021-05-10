package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.generic.discord.Command;
import com.faendir.zachtronics.bot.sc.SpaceChemMarker;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import net.dv8tion.jda.internal.entities.UserById;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertTrue;

@BotTest(SpaceChemMarker.SpaceChemConfiguration.class)
public class CommandTest {

    @Autowired
    private List<Command> commands;

    @Test
    public void testShow() {
        String text = "!show C fission I";
        String result = runCommand(text, null);
        assertTrue(result.contains("Fission I") && result.contains("C"));
    }

    @Test
    public void testSubmit() {
        String text = "!submit fission I (1/1/1) by testMan https://link";
        String result = runCommand(text, null);
        assertTrue(result.contains("Fission I") && result.contains("1/1/1"));
    }

    @Test
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
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @NotNull
    private String runCommand(String text, String attachmentContent) {
        List<Message.Attachment> attachments;
        if (attachmentContent != null) {
            Message.Attachment attachment = Mockito.mock(Message.Attachment.class);
            Mockito.when(attachment.retrieveInputStream()).thenReturn(
                    CompletableFuture.completedFuture(new ByteArrayInputStream(attachmentContent.getBytes())));
            attachments = Collections.singletonList(attachment);
        } else {
            attachments = Collections.emptyList();
        }

        Message message = new ReceivedMessage(-1, null, null, null, false, false, new TLongHashSet(), new TLongHashSet(),
                false, false, text, "", new UserById(295868901042946048L), null, null,
                null, Collections.emptyList(), attachments,
                Collections.emptyList(), 0);

        String result = commands.stream().filter(c -> text.startsWith("!" + c.getName())).findFirst().get()
                .handleMessage(message);
        System.out.println(result);
        return result;
    }
}