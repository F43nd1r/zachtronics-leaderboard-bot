package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.Game
import discord4j.core.`object`.entity.User
import discord4j.discordjson.json.ApplicationCommandOptionData

abstract class AbstractCommand : Command {
    override val isEnabled = true

    override val data by lazy {
        buildData()
    }

    override fun hasExecutionPermission(game: Game, user: User): Boolean {
        return isReadOnly || game.hasWritePermission(user)
    }

    abstract fun buildData(): ApplicationCommandOptionData
}