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
import com.faendir.zachtronics.bot.utils.Markdown;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Component
public class RepoCommand extends Command.Group {
    @Getter
    @RequiredArgsConstructor
    static class RepoLock {
        private final @NotNull GitRepository repo;
        private boolean locked = false;

        private void lock() {
            // yes, we just throw it away, as the thread that locked the repo would not be around later to unlock it,
            // may as well not store the lock at all, indeed this is sad, FIXME if you can
            repo.acquireReadAccess();
            locked = true;
        }
    }

    @Getter
    private final String name = "repo";
    @Getter
    private final String description = "Leaderboard repository management";

    /** repo name -> (repo, type, lock) */
    private final Map<String, @NotNull RepoLock> locks;

    private final CommandOption<String, String> repoOption;

    @Getter
    private final List<Command.Leaf> commands;

    public RepoCommand(@NotNull List<GitRepository> repositories) {
        // repositories come from spring, we need to init what depends on them in the constructor
        // as field initializers would run before the constructor and find a `null` repositories map
        locks = repositories.stream().collect(toMap(GitRepository::getName, RepoLock::new));
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
                embed.addField("", Markdown.link(repo.getName(), repo.getUrl()) + (repoLock.isLocked() ? " LOCKED" : ""), false);
            }
            return embed;
        }
    }

    @Getter
    class LockCommand extends Command.BasicLeaf {
        private final Secured secured = new DiscordUserSecured(DiscordUser.BOT_OWNERS);
        private final String name = "lock";
        private final String description = "Permanently lock selected repository from Writing, used for maintenance";
        private final List<CommandOption<?,?>> options = List.of(repoOption);

        @NotNull
        @Override
        public SafeMessageBuilder handleEvent(@NotNull ChatInputInteractionEvent event) {
            String repoName = repoOption.get(event);

            RepoLock repoLock = locks.get(repoName);
            if (!repoLock.isLocked()) {
                repoLock.lock();
                return new MultiMessageSafeEmbedMessageBuilder()
                    .title("Locked " + repoName)
                    .color(Colors.SUCCESS);
            }
            return new MultiMessageSafeEmbedMessageBuilder()
                .title(repoName + " was already locked")
                .color(Colors.UNCHANGED);
        }
    }
}