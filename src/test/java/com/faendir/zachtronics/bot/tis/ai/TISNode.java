/*
 * Copyright (c) 2026
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

package com.faendir.zachtronics.bot.tis.ai;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

@Value
class TISNode {
    int id;
    @NotNull List<TISLine> lines = new ArrayList<>();
    @NotNull List<TISLine> codeLines = new ArrayList<>();

    void marshalInto(@NonNull StringBuilder sb) {
        sb.append('@').append(id).append('\n');
        for (TISLine line: lines) {
            sb.append(line.getRawLine()).append('\n');
        }
        sb.append('\n');
    }
}
