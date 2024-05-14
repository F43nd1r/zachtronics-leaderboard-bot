/*
 * Copyright (c) 2024
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

import com.faendir.zachtronics.bot.discord.Colors;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder;
import com.faendir.zachtronics.bot.discord.command.security.DiscordUser;
import com.faendir.zachtronics.bot.discord.command.security.DiscordUserSecured;
import com.faendir.zachtronics.bot.discord.command.security.NotSecured;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.discord.embed.MultiMessageSafeEmbedMessageBuilder;
import com.faendir.zachtronics.bot.discord.embed.SafeMessageBuilder;
import com.faendir.zachtronics.bot.git.GitRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RepoCommand extends Command.Group {
    @Getter
    private final String name = "repo";
    @Getter
    private final String description = "Leaderboard repository management";

    private final Map<String, GitRepository> repositories;
    private final Map<String, GitRepository.ReadAccess> locks = new HashMap<>();

    private final CommandOption<String, GitRepository> repoOption;
    private final CommandOption<String, Class<? extends GitRepository.ReadAccess>> lockLevelOption =
        CommandOptionBuilder.string("lockLevel")
                            .description("Select lock type (defaults to write)")
                            .choices(Map.of("read", "r", "readWrite", "w"))
                            .convert((event, l) -> l.equals("r") ? GitRepository.ReadAccess.class : GitRepository.ReadWriteAccess.class)
                            .build();

    @Getter
    private final List<Command.Leaf> commands;

    public RepoCommand(@NotNull Map<String, GitRepository> repositories) {
        // repositories come from spring, we need to init what depends on them in the constructor
        // as field initializers would run before the constructor and find a `null` repositories map
        this.repositories = repositories;
        repoOption = CommandOptionBuilder.string("repo")
                                         .description("Select repository to lock")
                                         .choices(repositories.entrySet()
                                                              .stream()
                                                              .collect(Collectors.toMap(e -> e.getValue().getName(), Map.Entry::getKey)))
                                         .convert((event, k) -> this.repositories.get(k))
                                         .required()
                                         .build();
        commands = List.of(new InfoCommand(), new StateCommand(), new LockCommand(), new UnlockCommand());
    }

    class InfoCommand extends Command.BasicLeaf {
        @Getter
        private final Secured secured = NotSecured.INSTANCE;
        @Getter
        private final String name = "info";
        @Getter
        private final String description = "Information on game repo";
        @Getter
        private final List<CommandOption<?,?>> options = List.of(repoOption);

        @NotNull
        @Override
        public SafeMessageBuilder handleEvent(@NotNull ChatInputInteractionEvent event) {
            GitRepository repo = repoOption.get(event);
            return new MultiMessageSafeEmbedMessageBuilder()
                .title("Repository locks")
                .color(Colors.READ)
                .description(repo.getName() + "\n" + repo.getUrl());
        }
    }

    class StateCommand extends Command.BasicLeaf {
        @Getter
        private final Secured secured = NotSecured.INSTANCE;
        @Getter
        private final String name = "state";
        @Getter
        private final String description = "Query state of repo locks";
        @Getter
        private final List<CommandOption<?,?>> options = Collections.emptyList();

        @NotNull
        @Override
        public SafeMessageBuilder handleEvent(@NotNull ChatInputInteractionEvent event) {
            MultiMessageSafeEmbedMessageBuilder embed = new MultiMessageSafeEmbedMessageBuilder()
                .title("Repository locks")
                .color(Colors.READ);
            for (Map.Entry<String, GitRepository.ReadAccess> entry: locks.entrySet()) {
                embed.addField(entry.getKey(), entry.getValue().getClass().getName(), false);
            }
            return embed;
        }
    }

    class LockCommand extends Command.BasicLeaf {
        @Getter
        private final Secured secured = new DiscordUserSecured(DiscordUser.BOT_OWNERS);
        @Getter
        private final String name = "lock";
        @Getter
        private final String description = "Lock selected repository from read or writing, used for maintenance";
        @Getter
        private final List<CommandOption<?,?>> options = List.of(repoOption, lockLevelOption);

        @NotNull
        @Override
        public SafeMessageBuilder handleEvent(@NotNull ChatInputInteractionEvent event) {
            GitRepository repo = repoOption.get(event);
            Class<? extends GitRepository.ReadAccess> lockLevel = lockLevelOption.get(event);
            if (lockLevel == null)
                lockLevel = GitRepository.ReadWriteAccess.class;

            GitRepository.ReadAccess oldLock = locks.get(repo.getName());
            if (oldLock == null || !oldLock.getClass().equals(lockLevel)) {
                if (oldLock != null) {
                    oldLock.close();
                }
                locks.put(repo.getName(),
                          lockLevel.equals(GitRepository.ReadAccess.class) ? repo.acquireReadAccess() : repo.acquireWriteAccess());
                return new MultiMessageSafeEmbedMessageBuilder()
                    .title("Locked " + repo.getName())
                    .color(Colors.SUCCESS);
            }
            return new MultiMessageSafeEmbedMessageBuilder()
                .title(repo.getName() + "was already locked")
                .color(Colors.UNCHANGED);
        }
    }

    class UnlockCommand extends Command.BasicLeaf {
        @Getter
        private final Secured secured = new DiscordUserSecured(DiscordUser.BOT_OWNERS);
        @Getter
        private final String name = "unlock";
        @Getter
        private final String description = "Unlock selected repository from read or writing, used for maintenance";
        @Getter
        private final List<CommandOption<?,?>> options = List.of(repoOption);

        @NotNull
        @Override
        public SafeMessageBuilder handleEvent(@NotNull ChatInputInteractionEvent event) {
            GitRepository repo = repoOption.get(event);

            GitRepository.ReadAccess oldLock = locks.remove(repo.getName());
            if (oldLock != null) {
                oldLock.close();
                return new MultiMessageSafeEmbedMessageBuilder()
                    .title("Unlocked " + repo.getName())
                    .color(Colors.SUCCESS);
            }
            return new MultiMessageSafeEmbedMessageBuilder()
                .title(repo.getName() + " was already unlocked")
                .color(Colors.UNCHANGED);
        }
    }
}