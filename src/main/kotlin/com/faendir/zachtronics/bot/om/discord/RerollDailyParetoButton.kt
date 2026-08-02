/*
 * Copyright (c) 2026
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

package com.faendir.zachtronics.bot.om.discord

import com.faendir.zachtronics.bot.discord.StatelessComponent
import com.faendir.zachtronics.bot.discord.command.security.DiscordUser.Companion.OM_LB_ADMINS
import com.faendir.zachtronics.bot.discord.command.security.asDiscordUser
import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import discord4j.core.event.domain.interaction.ComponentInteractionEvent
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.Button
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
class RerollDailyParetoButton(private val omDailyPareto: OmDailyPareto) : StatelessComponent {
    override val id: String = ID

    override suspend fun trigger(event: ComponentInteractionEvent) {
        if (event is ButtonInteractionEvent) {
            val message = event.message.getOrNull() ?: return
            if (event.user.asDiscordUser() in OM_LB_ADMINS) {
                omDailyPareto.dailyParetoTask()
                message.edit()
                    .withComponentsOrNull(emptyList())
                    .awaitSingleOrNull()
            } else {
                event.reply().withEphemeral(true)
                    .withContent("Only leaderboard mods can reroll a daily pareto")
                    .awaitSingleOrNull()
            }
        }
    }

    companion object {
        const val ID = "reroll-daily-pareto"
        fun createAction() = ActionRow.of(Button.danger(ID, "Reroll"))
    }
}
