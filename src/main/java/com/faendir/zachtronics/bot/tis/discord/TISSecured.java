/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.tis.discord;

import com.faendir.zachtronics.bot.discord.command.security.*;

import java.util.Set;

public class TISSecured {
    private static final Set<DiscordUser> WIKI_ADMINS = Set.of(DiscordUser.IEEE12345, DiscordUser.BIGGIE);
    public static final DiscordUserSecured WIKI_ADMINS_ONLY = new DiscordUserSecured(WIKI_ADMINS);
    public static final Secured INSTANCE = SecuredKt.or(WIKI_ADMINS_ONLY, TrustedLeaderboardPosterRoleSecured.INSTANCE);

    private TISSecured() {}
}

