package com.faendir.zachtronics.bot.sc.discord.topic;

import com.faendir.zachtronics.bot.generic.discord.topic.StaticHelpTopic;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class CategoryHelp extends StaticHelpTopic {
    public CategoryHelp() {
        super("category", "The supported categories for researches are Cycles and Symbols, " +
                "ties broken by the other metric.\n" +
                "For productions we have in addition Cycles and Symbols at minimum reactors.\n" +
                "Boss levels are currently not supported.\n" +
                "\n" +
                "Each category also comes in a \"No bugs allowed\" and a " +
                "\"No precognition allowed\" version (for random puzzles), " +
                "whose record is very often equal to the unrestricted record.\n" +
                "\n" +
                "For further information see: " +
                "<https://www.reddit.com/r/spacechem/wiki/index#wiki_explanations>\n" +
                "\n" +
                "All categories are:\n" +
                Arrays.stream(ScCategory.values())
                        .map(c -> c.getDisplayName() + " (" + c.getContentDescription() + ")")
                        .collect(Collectors.joining("\n", "```", "```")));
    }
}
