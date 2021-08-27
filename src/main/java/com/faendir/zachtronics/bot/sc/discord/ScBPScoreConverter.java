package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.OptionConverter;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.command.Interaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class ScBPScoreConverter implements OptionConverter<ScScore> {
    @NotNull
    @Override
    public Mono<Optional<ScScore>> fromString(@NotNull InteractionCreateEvent context, @Nullable String s) {
        if(s == null) throw new IllegalArgumentException();
        return Mono.fromCallable(() -> Optional.of(makeScore(s)));
    }

    @NotNull
    private static ScScore makeScore(String rawScore) {
        ScScore score = ScScore.parseBPScore(rawScore);
        if (score == null)
            throw new IllegalArgumentException();
        return score;
    }
}
