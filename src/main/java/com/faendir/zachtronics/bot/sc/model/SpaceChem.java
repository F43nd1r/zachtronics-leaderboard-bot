package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.Game;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import discord4j.core.object.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SpaceChem implements Game {
    @Getter
    private final String displayName = "SpaceChem";
    @Getter
    private final String commandName = "sc";

    @NotNull
    public static ScPuzzle parsePuzzle(@NotNull String name) {
        return Arrays.stream(ScPuzzle.values())
                     .filter(p -> p.getDisplayName().equalsIgnoreCase(name))
                     .findFirst()
                     .orElseGet(() -> UtilsKt.getSingleMatchingPuzzle(ScPuzzle.values(), name));
    }

    private static final Set<Long> WIKI_ADMINS = Set.of(295868901042946048L, // 12345ieee,
                                                        516462621382410260L, // TT
                                                        185983061190508544L  // Zig
    );
    @NotNull
    @Override
    public Mono<Boolean> hasWritePermission(@Nullable User user) {
        if (user == null)
            return Mono.just(false);
        return Mono.just(WIKI_ADMINS.contains(user.getId().asLong()));
    }
}