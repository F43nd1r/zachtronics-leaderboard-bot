package com.faendir.zachtronics.bot.sz.discord;

import com.faendir.zachtronics.bot.discord.command.Command;
import com.faendir.zachtronics.bot.discord.command.GameCommand;
import com.faendir.zachtronics.bot.sz.SzQualifier;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SzCommand implements GameCommand {
    @Getter
    private final String displayName = "Shenzhen I/O";
    @Getter
    private final String commandName = "sz";
    @Getter
    private final List<Command> commands;

    public SzCommand(@SzQualifier List<Command> commands) {
        this.commands = commands;
    }
}
