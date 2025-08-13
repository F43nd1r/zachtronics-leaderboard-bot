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

package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder;
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;

public class ScOptionBuilders {
    public static final CommandOptionBuilder<String, ScPuzzle> PUZZLE_BUILDER =
        OptionHelpersKt.enumOptionBuilder("puzzle", ScPuzzle.class, ScPuzzle::getDisplayName)
                       .description("Puzzle name. Can be shortened or abbreviated. E.g. `sus beha`, `OPAS`");

    public static final CommandOptionBuilder<String, ScCategory> CATEGORY_BUILDER =
        OptionHelpersKt.enumOptionBuilder("category", ScCategory.class, ScCategory::getDisplayName)
                       .description("Category. E.g. `C`, `RS`");

    private ScOptionBuilders() {}
}
