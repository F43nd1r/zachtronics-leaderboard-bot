package com.faendir.zachtronics.bot.om.discord

import com.faendir.zachtronics.bot.discord.command.Command
import com.faendir.zachtronics.bot.discord.command.GameCommand
import com.faendir.zachtronics.bot.om.OmQualifier
import org.springframework.stereotype.Component

@Component
class OmCommand(@OmQualifier override val commands: List<Command>) : GameCommand {
    override val displayName = "Opus Magnum"
    override val commandName = "om"
}