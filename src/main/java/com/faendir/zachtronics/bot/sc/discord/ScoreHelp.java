package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.generic.discord.topic.StaticHelpTopic;
import org.springframework.stereotype.Component;

@Component
public class ScoreHelp extends StaticHelpTopic {
    public ScoreHelp() {
        super("score", "Scores are in the format cycles/reactors/symbols.\n" +
                "To mark a score as bugged in a submission append a `/B`, (`c/r/s/B`), " +
                "`/P` for precognition, `/BP` for both.");
    }
}
