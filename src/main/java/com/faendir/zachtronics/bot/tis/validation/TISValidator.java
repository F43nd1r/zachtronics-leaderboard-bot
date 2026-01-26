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

package com.faendir.zachtronics.bot.tis.validation;


import com.faendir.zachtronics.bot.tis.model.TISPuzzle;
import com.faendir.zachtronics.bot.tis.model.TISScore;
import com.faendir.zachtronics.bot.tis.model.TISType;
import com.faendir.zachtronics.bot.validation.ValidationException;
import org.checkerframework.checker.signedness.qual.Unsigned;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.EnumMap;
import java.util.Map;

/** Wrapper for libTIS100 from the TIS-100-CXX project */
public class TISValidator {
    private static final int CYCLES_LIMIT = 150_000;
    private static final int TOTAL_CYCLES_LIMIT = 100_000_000;
    private static final int RANDOM_TESTS = 100_000;

    private static final Map<TISPuzzle, String> CUSTOM_SPEC_CACHE = new EnumMap<>(TISPuzzle.class);

    private TISValidator() {}

    public static @NotNull TISScore validate(@NotNull String data, @NotNull TISPuzzle puzzle) throws ValidationException {
        if (puzzle.getType() == TISType.SANDBOX)
            throw new ValidationException("Sandbox levels are not supported");

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment sim = TISFFISim.tis_sim_create().reinterpret(arena, TISFFISim::tis_sim_destroy);
            TISFFISim.tis_sim_set_cycles_limit(sim, CYCLES_LIMIT);
            TISFFISim.tis_sim_set_total_cycles_limit(sim, TOTAL_CYCLES_LIMIT);
            for (int seed: puzzle.getExtraWitnessSeeds()) {
                TISFFISim.tis_sim_add_seed_range(sim, seed, seed + 1);
            }
            TISFFISim.tis_sim_add_seed_range(sim, 100000, 100000 + RANDOM_TESTS - puzzle.getExtraWitnessSeeds().length);

            if (puzzle.getId().startsWith("SPEC")) {
                String specName = puzzle.getId().replaceFirst("^SPEC", "");
                String customSpec = CUSTOM_SPEC_CACHE.computeIfAbsent(puzzle, _ -> {
                    ClassPathResource resource = new ClassPathResource("tis/custom/" + specName + ".lua");
                    try {
                        // do not cheat via a byte[], it needs to be null terminated
                        return new String(resource.getInputStream().readAllBytes());
                    }
                    catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
                @Unsigned int baseSeed = specName.matches("\\d+") ? Integer.parseUnsignedInt(specName) : 0;
                TISFFISim.tis_sim_set_custom_spec_code(sim, arena.allocateFrom(customSpec), baseSeed);
            }
            else {
                TISFFISim.tis_sim_set_builtin_level_name(sim, arena.allocateFrom(puzzle.getId()));
            }

            MemorySegment ffiScore = TISFFISim.tis_sim_simulate(sim, arena.allocateFrom(data));
            if (ffiScore.equals(MemorySegment.NULL) || !TISFFIScore.validated(ffiScore)) {
                String errorMessage = TISFFISim.tis_sim_get_error_message(sim).getString(0);
                throw new ValidationException("```\n" + errorMessage + "\n```");
            }

            return new TISScore((int) TISFFIScore.cycles(ffiScore),
                                (int) TISFFIScore.nodes(ffiScore),
                                (int) TISFFIScore.instructions(ffiScore),
                                TISFFIScore.achievement(ffiScore),
                                TISFFIScore.cheat(ffiScore),
                                TISFFIScore.hardcoded(ffiScore));
        }
    }
}
