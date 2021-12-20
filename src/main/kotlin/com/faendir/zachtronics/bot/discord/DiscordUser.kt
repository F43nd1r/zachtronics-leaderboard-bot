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

package com.faendir.zachtronics.bot.discord

import com.faendir.zachtronics.bot.discord.command.Secured
import discord4j.core.`object`.entity.User

enum class DiscordUser(val id: Long) {
    F43ND1R(288766560938622976L),
    IEEE12345(295868901042946048L), // aka 12345IEEE
    TT(516462621382410260L),
    ZIG(185983061190508544L),
    ;

    companion object {
        @JvmField
        val BOT_OWNERS = setOf(F43ND1R, IEEE12345)
    }
}

fun Set<DiscordUser>.contains(user: User) = any { it.id == user.id.asLong() }

class DiscordUserSecured(private val users: Set<DiscordUser>) : Secured {
    override fun hasExecutionPermission(user: User): Boolean = users.contains(user)
}