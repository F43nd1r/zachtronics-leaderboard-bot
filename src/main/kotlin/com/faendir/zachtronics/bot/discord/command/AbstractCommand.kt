package com.faendir.zachtronics.bot.discord.command

import discord4j.discordjson.json.ApplicationCommandOptionData

abstract class AbstractCommand : Command {
    override val data by lazy {
        buildData()
    }

    abstract fun buildData(): ApplicationCommandOptionData
}