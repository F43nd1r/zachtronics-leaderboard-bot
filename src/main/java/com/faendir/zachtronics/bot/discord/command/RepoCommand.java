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
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;
import com.faendir.zachtronics.bot.discord.command.security.DiscordUser;
import com.faendir.zachtronics.bot.discord.command.security.DiscordUserSecured;
import com.faendir.zachtronics.bot.discord.command.security.NotSecured;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.discord.embed.MultiMessageSafeEmbedMessageBuilder;
import com.faendir.zachtronics.bot.discord.embed.SafeMessageBuilder;
import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.utils.Markdown;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Component
public class RepoCommand extends Command.Group {
    @RequiredArgsConstructor
    private enum LockType {
        NONE("None", r -> null),
        WRITE("Write", GitRepository::acquireReadAccess),
        READWRITE("ReadWrite", GitRepository::acquireWriteAccess);

        @Getter
        private final String displayName;
        private final Function<GitRepository, GitRepository.ReadAccess> lockAction;

        private @NotNull RepoLock makeRepoLock(GitRepository repo) {
            return new RepoLock(repo, this, lockAction.apply(repo));
        }
    }
    
    @Value
    static class RepoLock implements Closeable {
        @NotNull GitRepository repo;
        @NotNull LockType lockType;
        @Nullable GitRepository.ReadAccess lock;

        @Override
        public void close() {
            if (lock != null) {
                lock.close();
            }
        }
    }

    @Getter
    private final String name = "repo";
    @Getter
    private final String description = "Leaderboard repository management";

    /** repo name -> (repo, type, lock) */
    private final Map<String, @NotNull RepoLock> locks;

    private final CommandOption<String, String> repoOption;
    private final CommandOption<String, LockType> lockTypeOption =
        OptionHelpersKt.enumOptionBuilder("locktype", LockType.class, LockType::getDisplayName)
                       .description("Select lock type (defaults to write)")
                       .build();

    @Getter
    private final List<Command.Leaf> commands;

    public RepoCommand(@NotNull List<GitRepository> repositories) {
        // repositories come from spring, we need to init what depends on them in the constructor
        // as field initializers would run before the constructor and find a `null` repositories map
        locks = repositories.stream().collect(toMap(GitRepository::getName, LockType.NONE::makeRepoLock));
        repoOption = CommandOptionBuilder.string("repo")
                                         .description("Select repository to lock")
                                         .choices(repositories.stream()
                                                              .collect(toMap(GitRepository::getName, GitRepository::getName)))
                                         .required()
                                         .build();
        commands = List.of(new StatusCommand(), new LockCommand());
    }

    @Getter
    class StatusCommand extends Command.BasicLeaf {
        private final Secured secured = NotSecured.INSTANCE;
        private final String name = "status";
        private final String description = "Query status of repos and locks";
        private final List<CommandOption<?,?>> options = Collections.emptyList();

        @NotNull
        @Override
        public SafeMessageBuilder handleEvent(@NotNull ChatInputInteractionEvent event) {
            MultiMessageSafeEmbedMessageBuilder embed = new MultiMessageSafeEmbedMessageBuilder()
                .title("Repositories")
                .color(Colors.READ);
            for (@NotNull RepoLock repoLock: locks.values()) {
                GitRepository repo = repoLock.getRepo();
                LockType lockType = repoLock.getLockType();
                embed.addField("", Markdown.link(repo.getName(), repo.getUrl()) + " Lock: " + lockType.getDisplayName(), false);
            }
            return embed;
        }
    }

    @Getter
    class LockCommand extends Command.BasicLeaf {
        private final Secured secured = new DiscordUserSecured(DiscordUser.BOT_OWNERS);
        private final String name = "lock";
        private final String description = "(Un)Lock selected repository from write or readWrite, used for maintenance";
        private final List<CommandOption<?,?>> options = List.of(repoOption, lockTypeOption);

        @NotNull
        @Override
        public SafeMessageBuilder handleEvent(@NotNull ChatInputInteractionEvent event) {
            String repoName = repoOption.get(event);
            LockType lockType = lockTypeOption.get(event);
            if (lockType == null)
                lockType = LockType.WRITE;

            RepoLock oldLock = locks.get(repoName);
            if (oldLock.getLockType() != lockType) {
                oldLock.close();
                GitRepository repo = oldLock.getRepo();
                locks.put(repoName, lockType.makeRepoLock(repo));
                return new MultiMessageSafeEmbedMessageBuilder()
                    .title((lockType == LockType.NONE ? "Unlocked " : "Locked ") + repoName)
                    .color(Colors.SUCCESS);
            }
            return new MultiMessageSafeEmbedMessageBuilder()
                .title(repoName + " lock state didn't change")
                .color(Colors.UNCHANGED);
        }
    }
}