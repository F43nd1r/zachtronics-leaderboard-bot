package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.discord.command.Command;
import com.faendir.zachtronics.bot.discord.command.GameCommand;
import com.faendir.zachtronics.bot.sc.ScQualifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScCommand implements GameCommand {
    @Getter
    private final String displayName = "SpaceChem";
    @Getter
    private final String commandName = "sc";
    @Getter
    @ScQualifier
    private final List<Command> commands;

}
