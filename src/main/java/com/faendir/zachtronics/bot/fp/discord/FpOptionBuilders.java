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

package com.faendir.zachtronics.bot.fp.discord;

import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder;
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;
import com.faendir.zachtronics.bot.fp.model.FpCategory;
import com.faendir.zachtronics.bot.fp.model.FpPuzzle;

public class FpOptionBuilders {
    public static final CommandOptionBuilder<String, FpPuzzle> PUZZLE_BUILDER =
        OptionHelpersKt.enumOptionBuilder("puzzle", FpPuzzle.class, FpPuzzle::getDisplayName)
                       .description("Puzzle name. Can be shortened or abbreviated. E.g. `1-1`, `add 2-3`");

    public static final CommandOptionBuilder<String, FpCategory> CATEGORY_BUILDER =
        OptionHelpersKt.enumOptionBuilder("category", FpCategory.class, FpCategory::getDisplayName)
                       .description("Category. E.g. `RCF`, `WFRC`");

    private FpOptionBuilders() {}
}
