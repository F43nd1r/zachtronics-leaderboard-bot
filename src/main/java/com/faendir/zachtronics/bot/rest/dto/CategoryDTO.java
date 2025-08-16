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

package com.faendir.zachtronics.bot.rest.dto;

import com.faendir.zachtronics.bot.model.Category;
import com.faendir.zachtronics.bot.model.Metric;
import com.faendir.zachtronics.bot.model.Type;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Value
public class CategoryDTO {
    @NotNull String id;
    @NotNull String displayName;
    @NotNull List<String> metrics;
    @NotNull List<String> puzzleTypes;

    @NotNull
    public static <C extends Enum<C> & Category> CategoryDTO fromCategory(@NotNull C category) {
        return new CategoryDTO(category.name(),
                               category.getDisplayName(),
                               category.getMetrics().stream().map(Metric::getDisplayName).toList(),
                               category.getSupportedTypes().stream().map(Type::getDisplayName).toList());
    }
}