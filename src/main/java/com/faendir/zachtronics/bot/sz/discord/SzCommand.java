package com.faendir.zachtronics.bot.sz.discord;

import com.faendir.zachtronics.bot.discord.command.Command;
import com.faendir.zachtronics.bot.discord.command.GameCommand;
import com.faendir.zachtronics.bot.sz.SzQualifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SzCommand implements GameCommand {
    @Getter
    private final String displayName = "Shenzhen I/O";
    @Getter
    private final String commandName = "sz";
    @Getter
    @SzQualifier
    private final List<Command> commands;
}
