package com.faendir.zachtronics.bot.generic.discord

import discord4j.discordjson.json.ApplicationCommandOptionData

abstract class AbstractCommand : Command {
    override val isEnabled = true

    override val data by lazy {
        buildData()
    }

    abstract fun buildData(): ApplicationCommandOptionData
}