package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.generic.discord.Command;
import com.faendir.zachtronics.bot.sc.SpaceChemMarker;
import com.faendir.zachtronics.bot.sc.model.SpaceChem;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import net.dv8tion.jda.internal.entities.UserById;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@BotTest(SpaceChemMarker.SpaceChemConfiguration.class)
public class CommandTest {

    @Autowired
    private List<Command> commands;
    @Autowired
    private SpaceChem spaceChem;

    @Test
    public void testHandleMessage() {
        String text = "!show C fission I";

        String result = runCommand(text);
        System.out.println(result);
        assertNotNull(result);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @NotNull
    private String runCommand(String text) {
        Message message = new ReceivedMessage(-1, null, null, false, false, new TLongHashSet(), new TLongHashSet(),
                                              false, false, text, "", new UserById(295868901042946048L), null, null,
                                              null, Collections.emptyList(), Collections.emptyList(),
                                              Collections.emptyList(), 0);

        return commands.stream().filter(c -> text.startsWith("!" + c.getName())).findFirst().get()
                       .handleMessage(message);
    }
}