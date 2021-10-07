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

    static boolean isWikiAdmin(@NotNull User user) {
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
