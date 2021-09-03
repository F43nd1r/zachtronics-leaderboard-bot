package com.faendir.zachtronics.bot.discord.command;

import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.WebhookExecuteRequest;
import discord4j.rest.util.MultipartRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Qualifier
class RestartCommand implements TopLevelCommand, Secured {
    @Getter
    String commandName = "restart";
    @Getter ApplicationContext applicationContext;

    @NotNull
    @Override
    public ApplicationCommandRequest buildRequest() {
        return ApplicationCommandRequest.builder()
                                        .name(commandName)
                                        .description("Stops the bot, which will restart with the latest image")
                                        .build();
    }

    @SneakyThrows
    @NotNull
    @Override
    public MultipartRequest<WebhookExecuteRequest> handle(@NotNull SlashCommandEvent event) {
        log.error("Requested shut down, see you soon");
        SpringApplication.exit(applicationContext);
        Thread.sleep(5000);
        System.exit(0);
        return null;
    }

    private final Set<Long> BOT_OWNERS = Set.of(295868901042946048L, // 12345ieee,
                                                288766560938622976L  // F43nd1r
    );

    @Override
    public boolean hasExecutionPermission(@NotNull User user) {
        return BOT_OWNERS.contains(user.getId().asLong());
    }
}