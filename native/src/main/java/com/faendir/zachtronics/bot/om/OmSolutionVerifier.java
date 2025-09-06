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

package com.faendir.zachtronics.bot.om;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class OmSolutionVerifier implements Closeable {
    static {
        NativeLoader.loadLibrary(OmSolutionVerifier.class.getClassLoader(), "verify-om");
    }

    private final Arena arena = Arena.ofConfined();
    private final MemorySegment verifier;

    public OmSolutionVerifier(byte[] puzzle, byte[] solution) {
        verifier = OmSimWrapper.verifier_create_from_bytes(
            toMemorySegment(puzzle), puzzle.length,
            toMemorySegment(solution), solution.length);
    }

    private @NotNull MemorySegment toMemorySegment(byte @NotNull [] array) {
        return arena.allocateFrom(ValueLayout.JAVA_BYTE, array);
    }


    private @NotNull MemorySegment toMemorySegment(@NotNull String s) {
        return arena.allocateFrom(s);
    }

    private static void throwIfError(MemorySegment verifier) {
        MemorySegment error = OmSimWrapper.verifier_error(verifier);
        if (!error.equals(MemorySegment.NULL)) {
            String errorString = error.getString(0);
            OmSimWrapper.verifier_error_clear(verifier);
            throw new OmSimException(errorString);
        }
    }

    public int getMetric(@NotNull OmSimMetric metric) {
        int result = OmSimWrapper.verifier_evaluate_metric(verifier, toMemorySegment(metric.getId()));
        throwIfError(verifier);
        return result;
    }

    public double getApproximateMetric(@NotNull OmSimMetric metric) {
        double result = OmSimWrapper.verifier_evaluate_approximate_metric(verifier, toMemorySegment(metric.getId()));
        throwIfError(verifier);
        return result;
    }

    @Override
    public void close() {
        OmSimWrapper.verifier_destroy(verifier);
        arena.close();
    }

}
