/*
 * Copyright (c) 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.discord.command.security.DiscordUser;
import com.faendir.zachtronics.bot.discord.command.security.DiscordUserKt;
import com.faendir.zachtronics.bot.discord.command.security.DiscordUserSecured;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.discord.command.security.SecuredKt;
import com.faendir.zachtronics.bot.discord.command.security.TrustedLeaderboardPosterRoleSecured;
import discord4j.core.object.entity.User;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ScSecured {
    public static Set<DiscordUser> WIKI_ADMINS = Set.of(DiscordUser.IEEE12345, DiscordUser.TT, DiscordUser.ZIG);
    public static Secured INSTANCE = SecuredKt.or(new DiscordUserSecured(WIKI_ADMINS), TrustedLeaderboardPosterRoleSecured.INSTANCE);

    public static boolean isWikiAdmin(@NotNull User user) {
        return DiscordUserKt.contains(WIKI_ADMINS, user);
    }
}
