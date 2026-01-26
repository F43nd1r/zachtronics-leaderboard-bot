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

package com.faendir.zachtronics.bot.kz.validation;

import com.faendir.zachtronics.bot.validation.NativeLoader;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.*;

public class KzSimFFIWrapper {
    private KzSimFFIWrapper() {}

    private static final AddressLayout C_POINTER = ValueLayout.ADDRESS
        .withTargetLayout(MemoryLayout.sequenceLayout(Long.MAX_VALUE, JAVA_BYTE));

    private static final Arena ARENA = Arena.global();
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LOOKUP = NativeLoader.loadLibrary("kaizensim", ARENA);

    private static final MethodHandle SCORE_CREATE = LINKER.downcallHandle(
        LOOKUP.find("score_create").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));

    private static final MethodHandle SCORE_DESTROY = LINKER.downcallHandle(
        LOOKUP.find("score_destroy").orElseThrow(),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

    private static final GroupLayout SCORE_RESULT_LAYOUT = MemoryLayout.structLayout(
        C_POINTER.withName("error"), // null terminated
        ValueLayout.JAVA_LONG.withName("error_len"),
        ValueLayout.JAVA_INT.withName("level"),
        ValueLayout.JAVA_INT.withName("time"),
        ValueLayout.JAVA_INT.withName("cost"),
        ValueLayout.JAVA_INT.withName("area"),
        ValueLayout.JAVA_BOOLEAN.withName("manipulated"),
        MemoryLayout.paddingLayout(7));

    private static void scoreDestroy(@NotNull MemorySegment score) {
        try {
            SCORE_DESTROY.invokeExact(score);
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public static MemorySegment scoreCreate(@NotNull Arena arena, byte @NotNull [] data) {
        MemorySegment ffiData = arena.allocateFrom(ValueLayout.JAVA_BYTE, data);
        return ((MemorySegment) SCORE_CREATE.invokeExact(ffiData, (long) data.length))
            .reinterpret(SCORE_RESULT_LAYOUT.byteSize(), arena, KzSimFFIWrapper::scoreDestroy);
    }

    private static final long ERROR_OFFSET = SCORE_RESULT_LAYOUT.byteOffset(PathElement.groupElement("error"));
    public static @Nullable String error(@NotNull MemorySegment score) {
        MemorySegment s = score.get(C_POINTER, ERROR_OFFSET);
        return s.equals(MemorySegment.NULL) ? null : s.getString(0);
    }

    private static final long COST_OFFSET = SCORE_RESULT_LAYOUT.byteOffset(PathElement.groupElement("cost"));
    public static int cost(@NotNull MemorySegment score) {
        return score.get(JAVA_INT, COST_OFFSET);
    }

    private static final long TIME_OFFSET = SCORE_RESULT_LAYOUT.byteOffset(PathElement.groupElement("time"));
    public static int time(@NotNull MemorySegment score) {
        return score.get(JAVA_INT, TIME_OFFSET);
    }

    private static final long AREA_OFFSET = SCORE_RESULT_LAYOUT.byteOffset(PathElement.groupElement("area"));
    public static int area(@NotNull MemorySegment score) {
        return score.get(JAVA_INT, AREA_OFFSET);
    }

    private static final long LEVEL_OFFSET = SCORE_RESULT_LAYOUT.byteOffset(PathElement.groupElement("level"));
    public static int level(@NotNull MemorySegment score) {
        return score.get(JAVA_INT, LEVEL_OFFSET);
    }

    private static final long MANIPULATED_OFFSET = SCORE_RESULT_LAYOUT.byteOffset(PathElement.groupElement("manipulated"));
    public static boolean manipulated(@NotNull MemorySegment score) {
        return score.get(JAVA_BOOLEAN, MANIPULATED_OFFSET);
    }

}
