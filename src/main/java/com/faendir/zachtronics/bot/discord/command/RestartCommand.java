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
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
class RestartCommand implements TopLevelCommand, Secured {
    @Getter
    private final String commandName = "restart";
    @Getter
    private final ApplicationContext applicationContext;

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