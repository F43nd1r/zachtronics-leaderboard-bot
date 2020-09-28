import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.discord.commands.Command;
import com.faendir.zachtronics.bot.model.sc.SpaceChem;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import net.dv8tion.jda.internal.entities.UserById;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableConfigurationProperties
@SpringBootTest(classes = Application.class, properties = "spring.main.lazy-initialization=true")
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

    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @NotNull
    private String runCommand(String text) {
        Message message = new ReceivedMessage(-1, null, null, false, false, new TLongHashSet(), new TLongHashSet(),
                                              false, false, text, "", new UserById(295868901042946048L), null, null,
                                              null, Collections.emptyList(), Collections.emptyList(),
                                              Collections.emptyList(), 0);

        return commands.stream().filter(c -> text.startsWith("!" + c.getName())).findFirst().get()
                       .handleMessage(spaceChem, message);
    }
}