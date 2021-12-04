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
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.io.File;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JNISolutionVerifier implements Closeable {
    static {
        NativeLoader.loadLibrary(JNISolutionVerifier.class.getClassLoader(), "omsim");
        NativeLoader.loadLibrary(JNISolutionVerifier.class.getClassLoader(), "native");
    }

    private final long verifier;

    public int getMetric(Metrics metric) {
        return JNISolutionVerifier.getMetric(verifier, metric.id);
    }

    @Override
    public void close() {
        JNISolutionVerifier.closeVerifier(verifier);
    }

    private static native long prepareVerifier(String puzzleFile, String solutionFile);

    private static native void closeVerifier(long verifier);

    private static native int getMetric(long verifier, String name);

    public static JNISolutionVerifier open(File puzzle, File solution) {
        return new JNISolutionVerifier(prepareVerifier(puzzle.getAbsolutePath(), solution.getAbsolutePath()));
    }

    public enum Metrics {
        // see verifier.h
        CYCLES("cycles"),
        COST("cost"),
        AREA("area (approximate)"),
        INSTRUCTIONS("instructions"),
        THROUGHPUT_CYCLES("throughput cycles"),
        THROUGHPUT_OUTPUTS("throughput outputs"),
        HEIGHT("height"),
        WIDTH_TIMES_TWO("width*2");
        private final String id;

        Metrics(String id) {
            this.id = id;
        }
    }
}
