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

package com.faendir.zachtronics.bot.kz.discord;

import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder;
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;
import com.faendir.zachtronics.bot.kz.model.KzCategory;
import com.faendir.zachtronics.bot.kz.model.KzPuzzle;
import org.jetbrains.annotations.NotNull;

public class KzOptionBuilders {
    public static @NotNull CommandOptionBuilder<String, KzPuzzle> puzzleOptionBuilder() {
        return OptionHelpersKt.enumOptionBuilder("puzzle", KzPuzzle.class, KzPuzzle::getDisplayName)
                              .description("Puzzle name. Can be shortened or abbreviated. E.g. `Corp Bin`, `WC`");
    }

    public static @NotNull CommandOptionBuilder<String, KzCategory> categoryOptionBuilder() {
        return OptionHelpersKt.enumOptionBuilder("category", KzCategory.class, KzCategory::getDisplayName)
                              .description("Category. E.g. `TC`, `CA`");
    }

    private KzOptionBuilders() {}
}
