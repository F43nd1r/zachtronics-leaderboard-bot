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

package com.faendir.zachtronics.bot.cw.discord;

import com.faendir.zachtronics.bot.cw.model.CwCategory;
import com.faendir.zachtronics.bot.cw.model.CwPuzzle;
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder;
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;

public class CwOptionBuilders {
    public static final CommandOptionBuilder<String, CwPuzzle> PUZZLE_BUILDER =
        OptionHelpersKt.enumOptionBuilder("puzzle", CwPuzzle.class, CwPuzzle::getDisplayName)
                       .description("Puzzle name. Can be shortened or abbreviated. E.g. `Sig Cross`, `SNR`");

    public static final CommandOptionBuilder<String, CwCategory> CATEGORY_BUILDER =
        OptionHelpersKt.enumOptionBuilder("category", CwCategory.class, CwCategory::getDisplayName)
                       .description("Category. E.g. `Size`, `Footprint`");

    private CwOptionBuilders() {}
}
