package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.discord.command.Secured;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface ScSecured extends Secured {
    Set<Long> WIKI_ADMINS = Set.of(295868901042946048L, // 12345ieee,
                                   516462621382410260L, // TT
                                   185983061190508544L  // Zig
    );

    private static boolean isWikiAdmin(@NotNull User user) {
        return WIKI_ADMINS.contains(user.getId().asLong());
    }

    private static boolean isTrustedPoster(@NotNull User user) {
        if (user instanceof Member)
            return ((Member) user).getRoles().any(r -> r.getName().equals("trusted-leaderboard-poster")).block();
        else
            return false;
    }

    @Override
    default boolean hasExecutionPermission(@NotNull User user) {
        return isWikiAdmin(user) || isTrustedPoster(user);
    }
}
