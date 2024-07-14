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

package com.faendir.zachtronics.bot.tis.model;

import com.faendir.zachtronics.bot.model.Puzzle;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Getter
public enum TISPuzzle implements Puzzle<TISCategory> {
    SELF_TEST_DIAGNOSTIC(TISGroup.TIS_100_SEGMENT_MAP, "00150", "SELF-TEST DIAGNOSTIC", "BUSY_LOOP", 83, 8),
    SIGNAL_AMPLIFIER(TISGroup.TIS_100_SEGMENT_MAP, "10981", "SIGNAL AMPLIFIER", null, 84, 4),
    DIFFERENTIAL_CONVERTER(TISGroup.TIS_100_SEGMENT_MAP, "20176", "DIFFERENTIAL CONVERTER", null, 100, 5),
    SIGNAL_COMPARATOR(TISGroup.TIS_100_SEGMENT_MAP, "21340", "SIGNAL COMPARATOR", "UNCONDITIONAL", 100, 6),
    SIGNAL_MULTIPLEXER(TISGroup.TIS_100_SEGMENT_MAP, "22280", "SIGNAL MULTIPLEXER", null, 100, 5),
    SEQUENCE_GENERATOR(TISGroup.TIS_100_SEGMENT_MAP, "30647", "SEQUENCE GENERATOR", null, 84, 4),
    SEQUENCE_COUNTER(TISGroup.TIS_100_SEGMENT_MAP, "31904", "SEQUENCE COUNTER", null /* NO_BACKUP is trivial, not tracked */, 84, 4),
    SIGNAL_EDGE_DETECTOR(TISGroup.TIS_100_SEGMENT_MAP, "32050", "SIGNAL EDGE DETECTOR", null, 84, 4),
    INTERRUPT_HANDLER(TISGroup.TIS_100_SEGMENT_MAP, "33762", "INTERRUPT HANDLER", null, 100, 6),
    SIMPLE_SANDBOX(TISGroup.TIS_100_SEGMENT_MAP, "USEG0", "SIMPLE SANDBOX", null, -1, -1),
    SIGNAL_PATTERN_DETECTOR(TISGroup.TIS_100_SEGMENT_MAP, "40196", "SIGNAL PATTERN DETECTOR", null, 82, 4),
    SEQUENCE_PEAK_DETECTOR(TISGroup.TIS_100_SEGMENT_MAP, "41427", "SEQUENCE PEAK DETECTOR", null, 84, 4),
    SEQUENCE_REVERSER(TISGroup.TIS_100_SEGMENT_MAP, "42656", "SEQUENCE REVERSER", "NO_MEMORY", 100, 3),
    SIGNAL_MULTIPLIER(TISGroup.TIS_100_SEGMENT_MAP, "43786", "SIGNAL MULTIPLIER", null, 100, 4),
    STACK_MEMORY_SANDBOX(TISGroup.TIS_100_SEGMENT_MAP, "USEG1", "STACK MEMORY SANDBOX", null, -1, -1),
    IMAGE_TEST_PATTERN_1(TISGroup.TIS_100_SEGMENT_MAP, "50370", "IMAGE TEST PATTERN 1", null, 1186, 1),
    IMAGE_TEST_PATTERN_2(TISGroup.TIS_100_SEGMENT_MAP, "51781", "IMAGE TEST PATTERN 2", null, 1150, 1),
    EXPOSURE_MASK_VIEWER(TISGroup.TIS_100_SEGMENT_MAP, "52544", "EXPOSURE MASK VIEWER", null, -1, 4),
    HISTOGRAM_VIEWER(TISGroup.TIS_100_SEGMENT_MAP, "53897", "HISTOGRAM VIEWER", null, -1, 4),
    IMAGE_CONSOLE_SANDBOX(TISGroup.TIS_100_SEGMENT_MAP, "USEG2", "IMAGE CONSOLE SANDBOX", null, -1, -1),
    SIGNAL_WINDOW_FILTER(TISGroup.TIS_100_SEGMENT_MAP, "60099", "SIGNAL WINDOW FILTER", null, 84, 4),
    SIGNAL_DIVIDER(TISGroup.TIS_100_SEGMENT_MAP, "61212", "SIGNAL DIVIDER", null, 100, 5),
    SEQUENCE_INDEXER(TISGroup.TIS_100_SEGMENT_MAP, "62711", "SEQUENCE INDEXER", null, 100, 4),
    SEQUENCE_SORTER(TISGroup.TIS_100_SEGMENT_MAP, "63534", "SEQUENCE SORTER", null, 100, 3),
    STORED_IMAGE_DECODER(TISGroup.TIS_100_SEGMENT_MAP, "70601", "STORED IMAGE DECODER", null, 1190, 4),

    UNKNOWN(TISGroup.TIS_100_SEGMENT_MAP, "UNKNOWN", "UNKNOWN", null, 84, 4),

    SEQUENCE_MERGER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.00.526.6", "SEQUENCE MERGER", null, 100, 6),
    INTEGER_SERIES_CALCULATOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.01.874.8", "INTEGER SERIES CALCULATOR", null, 100, 3),
    SEQUENCE_RANGE_LIMITER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.02.981.2", "SEQUENCE RANGE LIMITER", null, 100, 5),
    SIGNAL_ERROR_CORRECTOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.03.176.9", "SIGNAL ERROR CORRECTOR", null, 100, 5),
    SUBSEQUENCE_EXTRACTOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.04.340.5", "SUBSEQUENCE EXTRACTOR", null, 84, 4),
    SIGNAL_PRESCALER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.05.647.1", "SIGNAL PRESCALER", null, 100, 6),
    SIGNAL_AVERAGER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.06.786.0", "SIGNAL AVERAGER", null, 100, 4),
    SUBMAXIMUM_SELECTOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.07.050.0", "SUBMAXIMUM SELECTOR", null, 100, 6),
    DECIMAL_DECOMPOSER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.08.633.9", "DECIMAL DECOMPOSER", null, 100, 5),
    SEQUENCE_MODE_CALCULATOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.09.904.9", "SEQUENCE MODE CALCULATOR", null, 50, 3),
    SEQUENCE_NORMALIZER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.10.656.5", "SEQUENCE NORMALIZER", null, 100, 3),
    IMAGE_TEST_PATTERN_3(TISGroup.TIS_NET_DIRECTORY, "NEXUS.11.711.2", "IMAGE TEST PATTERN 3", null, 882, 1),
    IMAGE_TEST_PATTERN_4(TISGroup.TIS_NET_DIRECTORY, "NEXUS.12.534.4", "IMAGE TEST PATTERN 4", null, 1166, 1),
    SPATIAL_PATH_VIEWER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.13.370.9", "SPATIAL PATH VIEWER", null, 700, 4),
    CHARACTER_TERMINAL(TISGroup.TIS_NET_DIRECTORY, "NEXUS.14.781.3", "CHARACTER TERMINAL", null, 500, 4),
    BACK_REFERENCE_REIFIER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.15.897.9", "BACK-REFERENCE REIFIER", null, 100, 4),
    DYNAMIC_PATTERN_DETECTOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.16.212.8", "DYNAMIC PATTERN DETECTOR", null, 100, 5),
    SEQUENCE_GAP_INTERPOLATOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.17.135.0", "SEQUENCE GAP INTERPOLATOR", null, 84, 3),
    DECIMAL_TO_OCTAL_CONVERTER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.18.427.7", "DECIMAL TO OCTAL CONVERTER", null, 100, 4),
    PROLONGED_SEQUENCE_SORTER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.19.762.9", "PROLONGED SEQUENCE SORTER", null, 300, 3),
    PRIME_FACTOR_CALCULATOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.20.433.1", "PRIME FACTOR CALCULATOR", null, 100, 3),
    SIGNAL_EXPONENTIATOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.21.601.6", "SIGNAL EXPONENTIATOR", null, 100, 4),
    T20_NODE_EMULATOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.22.280.8", "T20 NODE EMULATOR", null, 100, 4),
    T31_NODE_EMULATOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.23.727.9", "T31 NODE EMULATOR", null, 100, 3),
    WAVE_COLLAPSE_SUPERVISOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.24.511.7", "WAVE COLLAPSE SUPERVISOR", null, 100, 6);

    private final TISGroup group;
    private final String id;
    private final String displayName;
    private final @Nullable String achievement;
    private final int legitCyclesBound;
    private final int legitNodesBound;
    private final TISType type;
    private final List<TISCategory> supportedCategories;
    private final String link;

    TISPuzzle(@NotNull TISGroup group, @NotNull String id, @NotNull String displayName, @Nullable String achievement, int legitCyclesBound,
              int legitNodesBound) {
        this.group = group;
        this.id = id;
        this.displayName = displayName;
        this.achievement = achievement;
        this.legitCyclesBound = legitCyclesBound;
        this.legitNodesBound = legitNodesBound;
        this.type = displayName.endsWith("SANDBOX") ? TISType.SANDBOX
                                                    : achievement != null ? TISType.WITH_ACHIEVEMENT
                                                                          : TISType.STANDARD;
        this.supportedCategories = achievement != null ? List.of(TISCategory.values())
                                                       : Arrays.stream(TISCategory.values())
                                                               .filter(c -> c.getAdmission() != TISMetric.ACHIEVEMENT)
                                                               .toList();
        this.link = "https://zlbb.faendir.com/tis/" + id;
    }

}
