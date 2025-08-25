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

package com.faendir.zachtronics.bot.tis.model;

import com.faendir.zachtronics.bot.model.Puzzle;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.faendir.zachtronics.bot.tis.model.TISGroup.*;
import static com.faendir.zachtronics.bot.tis.model.TISType.*;

@Getter
public enum TISPuzzle implements Puzzle<TISCategory> {
    SELF_TEST_DIAGNOSTIC(TIS_100_SEGMENT_MAP, "00150", "SELF-TEST DIAGNOSTIC", WITH_ACHIEVEMENT),
    SIGNAL_AMPLIFIER(TIS_100_SEGMENT_MAP, "10981", "SIGNAL AMPLIFIER", STANDARD),
    DIFFERENTIAL_CONVERTER(TIS_100_SEGMENT_MAP, "20176", "DIFFERENTIAL CONVERTER", STANDARD),
    SIGNAL_COMPARATOR(TIS_100_SEGMENT_MAP, "21340", "SIGNAL COMPARATOR", WITH_ACHIEVEMENT),
    SIGNAL_MULTIPLEXER(TIS_100_SEGMENT_MAP, "22280", "SIGNAL MULTIPLEXER", STANDARD),
    SEQUENCE_GENERATOR(TIS_100_SEGMENT_MAP, "30647", "SEQUENCE GENERATOR", STANDARD),
    SEQUENCE_COUNTER(TIS_100_SEGMENT_MAP, "31904", "SEQUENCE COUNTER", STANDARD),
    SIGNAL_EDGE_DETECTOR(TIS_100_SEGMENT_MAP, "32050", "SIGNAL EDGE DETECTOR", STANDARD, 200009, 200108, 201545),
    INTERRUPT_HANDLER(TIS_100_SEGMENT_MAP, "33762", "INTERRUPT HANDLER", STANDARD),
    SIMPLE_SANDBOX(TIS_100_SEGMENT_MAP, "USEG0", "SIMPLE SANDBOX", SANDBOX),
    SIGNAL_PATTERN_DETECTOR(TIS_100_SEGMENT_MAP, "40196", "SIGNAL PATTERN DETECTOR", STANDARD),
    SEQUENCE_PEAK_DETECTOR(TIS_100_SEGMENT_MAP, "41427", "SEQUENCE PEAK DETECTOR", STANDARD, 200005),
    SEQUENCE_REVERSER(TIS_100_SEGMENT_MAP, "42656", "SEQUENCE REVERSER", WITH_ACHIEVEMENT),
    SIGNAL_MULTIPLIER(TIS_100_SEGMENT_MAP, "43786", "SIGNAL MULTIPLIER", STANDARD),
    STACK_MEMORY_SANDBOX(TIS_100_SEGMENT_MAP, "USEG1", "STACK MEMORY SANDBOX", SANDBOX),
    IMAGE_TEST_PATTERN_1(TIS_100_SEGMENT_MAP, "50370", "IMAGE TEST PATTERN 1", FIXED_OUTPUT),
    IMAGE_TEST_PATTERN_2(TIS_100_SEGMENT_MAP, "51781", "IMAGE TEST PATTERN 2", FIXED_OUTPUT),
    EXPOSURE_MASK_VIEWER(TIS_100_SEGMENT_MAP, "52544", "EXPOSURE MASK VIEWER", STANDARD, 200014),
    HISTOGRAM_VIEWER(TIS_100_SEGMENT_MAP, "53897", "HISTOGRAM VIEWER", STANDARD),
    IMAGE_CONSOLE_SANDBOX(TIS_100_SEGMENT_MAP, "USEG2", "IMAGE CONSOLE SANDBOX", SANDBOX),
    SIGNAL_WINDOW_FILTER(TIS_100_SEGMENT_MAP, "60099", "SIGNAL WINDOW FILTER", STANDARD),
    SIGNAL_DIVIDER(TIS_100_SEGMENT_MAP, "61212", "SIGNAL DIVIDER", STANDARD),
    SEQUENCE_INDEXER(TIS_100_SEGMENT_MAP, "62711", "SEQUENCE INDEXER", STANDARD),
    SEQUENCE_SORTER(TIS_100_SEGMENT_MAP, "63534", "SEQUENCE SORTER", STANDARD,
                    200132, 200206, 200308, 200578, 202012, 202927, 205591, 207089, 207480,
                    215493, 216246, 222497, 224109, 265724, 282624,
                    300280, 309856, 357831, 449198, 488297, 617519, 661244, 901742, 1313389, 1837283, 1822200, 1900806),
    STORED_IMAGE_DECODER(TIS_100_SEGMENT_MAP, "70601", "STORED IMAGE DECODER", STANDARD, 200032, 2955698),

    UNKNOWN(TIS_100_SEGMENT_MAP, "UNKNOWN", "UNKNOWN", STANDARD),

    SEQUENCE_MERGER(TIS_NET_DIRECTORY, "NEXUS.00.526.6", "SEQUENCE MERGER", STANDARD),
    INTEGER_SERIES_CALCULATOR(TIS_NET_DIRECTORY, "NEXUS.01.874.8", "INTEGER SERIES CALCULATOR", STANDARD),
    SEQUENCE_RANGE_LIMITER(TIS_NET_DIRECTORY, "NEXUS.02.981.2", "SEQUENCE RANGE LIMITER", STANDARD),
    SIGNAL_ERROR_CORRECTOR(TIS_NET_DIRECTORY, "NEXUS.03.176.9", "SIGNAL ERROR CORRECTOR", STANDARD),
    SUBSEQUENCE_EXTRACTOR(TIS_NET_DIRECTORY, "NEXUS.04.340.5", "SUBSEQUENCE EXTRACTOR", STANDARD),
    SIGNAL_PRESCALER(TIS_NET_DIRECTORY, "NEXUS.05.647.1", "SIGNAL PRESCALER", STANDARD, 270276),
    SIGNAL_AVERAGER(TIS_NET_DIRECTORY, "NEXUS.06.786.0", "SIGNAL AVERAGER", STANDARD, 202116, 203279, 203483, 211527, 212654),
    SUBMAXIMUM_SELECTOR(TIS_NET_DIRECTORY, "NEXUS.07.050.0", "SUBMAXIMUM SELECTOR", STANDARD, 200009, 200031, 200713, 201163),
    DECIMAL_DECOMPOSER(TIS_NET_DIRECTORY, "NEXUS.08.633.9", "DECIMAL DECOMPOSER", STANDARD, 200006, 200030, 200053, 204958),
    SEQUENCE_MODE_CALCULATOR(TIS_NET_DIRECTORY, "NEXUS.09.904.9", "SEQUENCE MODE CALCULATOR", STANDARD,
                             200029, 200038, 200385, 201529, 201550, 205321, 208015, 210354, 210952, 291947),
    SEQUENCE_NORMALIZER(TIS_NET_DIRECTORY, "NEXUS.10.656.5", "SEQUENCE NORMALIZER", STANDARD,
                        200005, 200022, 200122, 200199, 200222, 203080, 205237, 209353, 217852, 224884, 283860, 329689),
    IMAGE_TEST_PATTERN_3(TIS_NET_DIRECTORY, "NEXUS.11.711.2", "IMAGE TEST PATTERN 3", FIXED_OUTPUT),
    IMAGE_TEST_PATTERN_4(TIS_NET_DIRECTORY, "NEXUS.12.534.4", "IMAGE TEST PATTERN 4", FIXED_OUTPUT),
    SPATIAL_PATH_VIEWER(TIS_NET_DIRECTORY, "NEXUS.13.370.9", "SPATIAL PATH VIEWER", STANDARD),
    CHARACTER_TERMINAL(TIS_NET_DIRECTORY, "NEXUS.14.781.3", "CHARACTER TERMINAL", STANDARD),
    BACK_REFERENCE_REIFIER(TIS_NET_DIRECTORY, "NEXUS.15.897.9", "BACK-REFERENCE REIFIER", STANDARD),
    DYNAMIC_PATTERN_DETECTOR(TIS_NET_DIRECTORY, "NEXUS.16.212.8", "DYNAMIC PATTERN DETECTOR", STANDARD, 201039, 209508),
    SEQUENCE_GAP_INTERPOLATOR(TIS_NET_DIRECTORY, "NEXUS.17.135.0", "SEQUENCE GAP INTERPOLATOR", STANDARD),
    DECIMAL_TO_OCTAL_CONVERTER(TIS_NET_DIRECTORY, "NEXUS.18.427.7", "DECIMAL TO OCTAL CONVERTER", STANDARD, 200002),
    PROLONGED_SEQUENCE_SORTER(TIS_NET_DIRECTORY, "NEXUS.19.762.9", "PROLONGED SEQUENCE SORTER", STANDARD, 200033, 1915341),
    PRIME_FACTOR_CALCULATOR(TIS_NET_DIRECTORY, "NEXUS.20.433.1", "PRIME FACTOR CALCULATOR", STANDARD),
    SIGNAL_EXPONENTIATOR(TIS_NET_DIRECTORY, "NEXUS.21.601.6", "SIGNAL EXPONENTIATOR", STANDARD, 207405, 244781, 4189154),
    T20_NODE_EMULATOR(TIS_NET_DIRECTORY, "NEXUS.22.280.8", "T20 NODE EMULATOR", STANDARD),
    T31_NODE_EMULATOR(TIS_NET_DIRECTORY, "NEXUS.23.727.9", "T31 NODE EMULATOR", STANDARD, 200119, 200368, 200391, 218959),
    WAVE_COLLAPSE_SUPERVISOR(TIS_NET_DIRECTORY, "NEXUS.24.511.7", "WAVE COLLAPSE SUPERVISOR", STANDARD, 200009, 200695, 202001),

    NUMERAL_AMALGAMATOR(TOURNAMENT_2018, "SPEC49083185", "NUMERAL AMALGAMATOR", STANDARD),
    IMAGE_TEST_PATTERN_HWAIR(TOURNAMENT_2018, "SPEC65521532", "IMAGE TEST PATTERN: HWAIR", FIXED_OUTPUT),
    UNDECIMAL_TWIN_ROUNDER(TOURNAMENT_2018, "SPEC44149538", "UNDECIMAL TWIN ROUNDER", STANDARD),
    TRI_SEQUENCE_IDENTIFIER(TOURNAMENT_2018, "SPEC26159035", "TRI-SEQUENCE IDENTIFIER", STANDARD),
    STRIATION_PLOTTER(TOURNAMENT_2018, "SPEC35049542", "STRIATION PLOTTER", STANDARD, 205338),
    OUT_OF_RANGE_DIGIT_VIEWER_A(TOURNAMENT_2018, "SPEC28320828", "OUT-OF-RANGE DIGIT VIEWER A", STANDARD),
    INTERSECTION_IDENTIFIER(TOURNAMENT_2018, "SPEC64160260", "INTERSECTION IDENTIFIER", STANDARD),
    ERROR_8_79324720(TOURNAMENT_2018, "SPEC79324720", "ERROR: 8 `+.$_ - 79324720", STANDARD),
    ;

    private final TISGroup group;
    private final String id;
    private final String displayName;
    private final TISType type;
    private final @Nullable String achievement;
    private final List<TISCategory> supportedCategories;
    private final int[] extraWitnessSeeds;
    private final String link;

    TISPuzzle(@NotNull TISGroup group, @NotNull String id, @NotNull String displayName, @NotNull TISType type, int... extraWitnessSeeds) {
        this.group = group;
        this.id = id;
        this.displayName = displayName;
        this.achievement = switch (id) { // sadly `this` doesn't exist yet to switch on
            case "00150" -> "BUSY_LOOP";
            case "21340" -> "UNCONDITIONAL";
            /* case "31904" -> "NO_BACKUP"; trivial, not tracked */
            case "42656" -> "NO_MEMORY";
            default -> null;
        };
        this.type = type;
        this.supportedCategories = switch (type) {
            case STANDARD -> Arrays.stream(TISCategory.values())
                                   .filter(c -> !c.getAdmission().getDisplayName().contains("a"))
                                   .toList();
            case WITH_ACHIEVEMENT -> List.of(TISCategory.values());
            case FIXED_OUTPUT -> Arrays.stream(TISCategory.values())
                                       .filter(c -> c.getAdmission() == TISMetric.NOT_CHEATING)
                                       .toList();
            case SANDBOX -> Collections.emptyList();
        };
        this.extraWitnessSeeds = extraWitnessSeeds;
        this.link = "https://zlbb.faendir.com/tis/" + id;
    }

    private static Path customSpecDir = null;

    public boolean isCustomSpec() {
        return id.startsWith("SPEC");
    }

    public synchronized @NotNull Path extractCustomSpec() throws IOException {
        assert isCustomSpec();
        if (customSpecDir == null) {
            customSpecDir = Files.createTempDirectory("tis_custom_specs");
            customSpecDir.toFile().deleteOnExit();
        }
        String specFileName = id.replace("SPEC", "") + ".lua";
        Path specPath = customSpecDir.resolve(specFileName);
        if (!Files.exists(specPath)) {
            ClassPathResource resource = new ClassPathResource("tis/custom/" + specFileName);
            Files.copy(resource.getInputStream(), specPath);
        }
        return specPath;
    }

}
