/*
 * Copyright (c) 2023
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

package com.faendir.zachtronics.bot.inf.validation;

import com.faendir.zachtronics.bot.inf.model.IfScore;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class holds the properties of a specific save slot, custom extensions included
 */
@Data
class IfSolutionInfo {
    private static final Pattern FLAGS_REGEX = Pattern.compile("/?" + IfScore.FLAGS_REGEX);

    Integer inputRate;
    Integer blocks;
    Integer cycles;
    Integer footprint;
    String solution;

    boolean outOfBounds = false;
    @Accessors(fluent = true)
    boolean usesGRA = true;
    boolean finite = true;

    String author;

    boolean hasData() {
        return inputRate != null && solution != null;
    }

    boolean hasScore() {
        return blocks != null && cycles != null && footprint != null;
    }

    public void loadFlags(String flags) {
        Matcher m = FLAGS_REGEX.matcher(flags);
        if (m.matches()) {
            outOfBounds = m.group("Oflag") != null;
            usesGRA = m.group("GRAflag") != null;
            finite = m.group("Fflag") != null;
        }
    }
}
