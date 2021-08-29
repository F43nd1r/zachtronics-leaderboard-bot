package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.OptionConverter;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

public class ScBPScoreConverter implements OptionConverter<ScScore> {
    @NotNull
    @Override
    public ScScore fromString(@NotNull SlashCommandEvent context, @NotNull String s) {
        return makeScore(s);
    }

    @NotNull
    private static ScScore makeScore(String rawScore) {
        ScScore score = ScScore.parseBPScore(rawScore);
        if (score == null)
            throw new IllegalArgumentException();
        return score;
    }
}
