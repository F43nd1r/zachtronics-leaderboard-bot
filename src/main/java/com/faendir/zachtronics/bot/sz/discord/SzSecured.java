package com.faendir.zachtronics.bot.sz.discord;

import com.faendir.zachtronics.bot.discord.command.Secured;
import discord4j.core.object.entity.User;
import org.jetbrains.annotations.NotNull;

public interface SzSecured extends Secured {
    long WIKI_ADMIN = 295868901042946048L; // 12345ieee

    @Override
    default boolean hasExecutionPermission(@NotNull User user) {
        return user.getId().asLong() == WIKI_ADMIN;
    }
}
