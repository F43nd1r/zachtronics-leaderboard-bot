/*
 * Copyright (c) 2025
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

package com.faendir.zachtronics.bot.sz.discord;

import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder;
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;
import com.faendir.zachtronics.bot.sz.model.SzCategory;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import org.jetbrains.annotations.NotNull;

public class SzOptionBuilders {
    public static @NotNull CommandOptionBuilder<String, SzPuzzle> puzzleOptionBuilder() {
        return OptionHelpersKt.enumOptionBuilder("puzzle", SzPuzzle.class, SzPuzzle::getDisplayName)
                              .description("Puzzle name. Can be shortened or abbreviated. E.g. `fake surv`, `HD`");
    }

    public static @NotNull CommandOptionBuilder<String, SzCategory> categoryOptionBuilder() {
        return OptionHelpersKt.enumOptionBuilder("category", SzCategory.class, SzCategory::getDisplayName)
                              .description("Category. E.g. `CP`, `LC`");
    }

    private SzOptionBuilders() {}
}
