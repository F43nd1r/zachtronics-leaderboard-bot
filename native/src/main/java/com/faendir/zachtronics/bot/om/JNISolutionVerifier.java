/*
 * Copyright (c) 2021
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

package com.faendir.zachtronics.bot.om;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JNISolutionVerifier implements Closeable {
    static {
        NativeLoader.loadLibrary(JNISolutionVerifier.class.getClassLoader(), "omsim");
        NativeLoader.loadLibrary(JNISolutionVerifier.class.getClassLoader(), "native");
    }

    private final byte[] puzzle;
    private final byte[] solution;
    private Long verifier = null;

    public static native String getPuzzleNameFromSolution(byte[] solution);

    private static native long prepareVerifier(byte[] puzzle, byte[] solution);

    private static native void closeVerifier(long verifier);

    private static native int getMetric(long verifier, String name);

    public static JNISolutionVerifier open(byte[] puzzle, byte[] solution) {
        return new JNISolutionVerifier(puzzle, solution);
    }

    public int getMetric(Metrics metric) {
        if (verifier == null) verifier = prepareVerifier(puzzle, solution);
        try {
            return getMetric(verifier, metric.id);
        } catch (Throwable t) {
            close();
            throw t;
        }
    }

    @Override
    public void close() {
        if (verifier != null) {
            closeVerifier(verifier);
            verifier = null;
        }
    }

    public enum Metrics {
        // see verifier.h
        PARSED_CYCLES("parsed cycles"),
        PARSED_COST("parsed cost"),
        PARSED_AREA("parsed area"),
        PARSED_INSTRUCTIONS("parsed instructions"),
        PARTS_OF_TYPE_TRACK("parts of type track"),
        PARTS_OF_TYPE_BARON("parts of type baron"),
        PARTS_OF_TYPE_GLYPH_DISPOSAL("parts of type glyph-disposal"),
        MAXIMUM_ABSOLUTE_ARM_ROTATION("maximum absolute arm rotation"),
        OVERLAP("overlap"),
        DUPLICATE_REAGENTS("duplicate reagents"),
        DUPLICATE_PRODUCTS("duplicate products"),
        MAXIMUM_TRACK_GAP_POW_2("maximum track gap^2"),
        COST("cost"),
        INSTRUCTIONS("instructions"),
        CYCLES("cycles"),
        AREA_APPROXIMATE("area (approximate)"),
        THROUGHPUT_CYCLES("throughput cycles"),
        THROUGHPUT_OUTPUTS("throughput outputs"),
        THROUGHPUT_WASTE("throughput waste"),
        HEIGHT("height"),
        WIDTH_TIMES_TWO("width*2"),
        ;
        private final String id;

        Metrics(String id) {
            this.id = id;
        }
    }
}
