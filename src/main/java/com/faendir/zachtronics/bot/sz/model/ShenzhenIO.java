package com.faendir.zachtronics.bot.sz.model;

import com.faendir.zachtronics.bot.model.Game;
import com.faendir.zachtronics.bot.model.Leaderboard;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import discord4j.core.object.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ShenzhenIO implements Game {
    @Getter
    private final String displayName = "Shenzhen I/O";
    @Getter
    private final String commandName = "sz";
    @Getter
    private final List<Leaderboard<SzCategory, SzPuzzle, SzRecord>> leaderboards;

    @NotNull
    public static SzPuzzle parsePuzzle(@NotNull String name) {
        return UtilsKt.getSingleMatchingPuzzle(SzPuzzle.values(), name);
    }

    private static final long WIKI_ADMIN = 295868901042946048L; // 12345ieee
    @NotNull
    @Override
    public Mono<Boolean> hasWritePermission(@Nullable User user) {
        if (user == null)
            return Mono.just(false);
        return Mono.just(user.getId().asLong() == WIKI_ADMIN);
    }
}