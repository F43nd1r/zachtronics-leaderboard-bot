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

package com.faendir.zachtronics.bot.om.validation;

import com.faendir.zachtronics.bot.validation.NativeLoader;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

/**
 * Omsim FFM wrapper, regenerate if omsim changes its public API via:
 * * download jextract from <a href="https://jdk.java.net/jextract/">here</a>
 * * run `./jextract omsim/verifier.h`
 * * replace the default `SYMBOL_LOOKUP` with `NativeLoader.loadLibrary("verify-om", LIBRARY_ARENA);`
 * * remove the unused downcall tracer (optional)
 * * add `@SuppressWarnings("unused")` (optional)
 */
@SuppressWarnings("unused")
public class OmSimWrapper {

    OmSimWrapper() {
        // Should not be called directly
    }

    static final Arena LIBRARY_ARENA = Arena.ofAuto();
    static final SymbolLookup SYMBOL_LOOKUP = NativeLoader.loadLibrary("verify-om", LIBRARY_ARENA);

    static MemorySegment findOrThrow(String symbol) {
        return SYMBOL_LOOKUP.find(symbol).orElseThrow(() -> new UnsatisfiedLinkError("unresolved symbol: " + symbol));
    }

    static MethodHandle upcallHandle(Class<?> fi, String name, FunctionDescriptor fdesc) {
        try {
            return MethodHandles.lookup().findVirtual(fi, name, fdesc.toMethodType());
        }
        catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    static MemoryLayout align(MemoryLayout layout, long align) {
        return switch (layout) {
            case PaddingLayout p -> p;
            case ValueLayout v -> v.withByteAlignment(align);
            case GroupLayout g -> {
                MemoryLayout[] alignedMembers = g.memberLayouts().stream().map(m -> align(m, align)).toArray(MemoryLayout[]::new);
                yield g instanceof StructLayout ? MemoryLayout.structLayout(alignedMembers) : MemoryLayout.unionLayout(alignedMembers);
            }
            case SequenceLayout s -> MemoryLayout.sequenceLayout(s.elementCount(), align(s.elementLayout(), align));
        };
    }

    public static final ValueLayout.OfBoolean C_BOOL = ValueLayout.JAVA_BOOLEAN;
    public static final ValueLayout.OfByte C_CHAR = ValueLayout.JAVA_BYTE;
    public static final ValueLayout.OfShort C_SHORT = ValueLayout.JAVA_SHORT;
    public static final ValueLayout.OfInt C_INT = ValueLayout.JAVA_INT;
    public static final ValueLayout.OfLong C_LONG_LONG = ValueLayout.JAVA_LONG;
    public static final ValueLayout.OfFloat C_FLOAT = ValueLayout.JAVA_FLOAT;
    public static final ValueLayout.OfDouble C_DOUBLE = ValueLayout.JAVA_DOUBLE;
    public static final AddressLayout C_POINTER = ValueLayout.ADDRESS.withTargetLayout(
            MemoryLayout.sequenceLayout(java.lang.Long.MAX_VALUE, JAVA_BYTE));
    public static final ValueLayout.OfLong C_LONG = ValueLayout.JAVA_LONG;

    private static class verifier_find_puzzle_name_in_solution_bytes {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_POINTER, OmSimWrapper.C_POINTER,
                                                                            OmSimWrapper.C_INT, OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_find_puzzle_name_in_solution_bytes");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * const char *verifier_find_puzzle_name_in_solution_bytes(const char *solution_bytes, int solution_length, int *name_length)
     *}
     */
    public static FunctionDescriptor verifier_find_puzzle_name_in_solution_bytes$descriptor() {
        return verifier_find_puzzle_name_in_solution_bytes.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * const char *verifier_find_puzzle_name_in_solution_bytes(const char *solution_bytes, int solution_length, int *name_length)
     *}
     */
    public static MethodHandle verifier_find_puzzle_name_in_solution_bytes$handle() {
        return verifier_find_puzzle_name_in_solution_bytes.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * const char *verifier_find_puzzle_name_in_solution_bytes(const char *solution_bytes, int solution_length, int *name_length)
     *}
     */
    public static MemorySegment verifier_find_puzzle_name_in_solution_bytes$address() {
        return verifier_find_puzzle_name_in_solution_bytes.ADDR;
    }

    /**
     * {@snippet lang = c:
     * const char *verifier_find_puzzle_name_in_solution_bytes(const char *solution_bytes, int solution_length, int *name_length)
     *}
     */
    public static MemorySegment verifier_find_puzzle_name_in_solution_bytes(MemorySegment solution_bytes, int solution_length,
                                                                            MemorySegment name_length) {
        var mh$ = verifier_find_puzzle_name_in_solution_bytes.HANDLE;
        try {
            return (MemorySegment) mh$.invokeExact(solution_bytes, solution_length, name_length);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_create {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_POINTER, OmSimWrapper.C_POINTER,
                                                                            OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_create");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * void *verifier_create(const char *puzzle_filename, const char *solution_filename)
     *}
     */
    public static FunctionDescriptor verifier_create$descriptor() {
        return verifier_create.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * void *verifier_create(const char *puzzle_filename, const char *solution_filename)
     *}
     */
    public static MethodHandle verifier_create$handle() {
        return verifier_create.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * void *verifier_create(const char *puzzle_filename, const char *solution_filename)
     *}
     */
    public static MemorySegment verifier_create$address() {
        return verifier_create.ADDR;
    }

    /**
     * {@snippet lang = c:
     * void *verifier_create(const char *puzzle_filename, const char *solution_filename)
     *}
     */
    public static MemorySegment verifier_create(MemorySegment puzzle_filename, MemorySegment solution_filename) {
        var mh$ = verifier_create.HANDLE;
        try {
            return (MemorySegment) mh$.invokeExact(puzzle_filename, solution_filename);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_create_from_bytes {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_POINTER, OmSimWrapper.C_POINTER,
                                                                            OmSimWrapper.C_INT, OmSimWrapper.C_POINTER, OmSimWrapper.C_INT);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_create_from_bytes");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * void *verifier_create_from_bytes(const char *puzzle_bytes, int puzzle_length, const char *solution_bytes, int solution_length)
     *}
     */
    public static FunctionDescriptor verifier_create_from_bytes$descriptor() {
        return verifier_create_from_bytes.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * void *verifier_create_from_bytes(const char *puzzle_bytes, int puzzle_length, const char *solution_bytes, int solution_length)
     *}
     */
    public static MethodHandle verifier_create_from_bytes$handle() {
        return verifier_create_from_bytes.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * void *verifier_create_from_bytes(const char *puzzle_bytes, int puzzle_length, const char *solution_bytes, int solution_length)
     *}
     */
    public static MemorySegment verifier_create_from_bytes$address() {
        return verifier_create_from_bytes.ADDR;
    }

    /**
     * {@snippet lang = c:
     * void *verifier_create_from_bytes(const char *puzzle_bytes, int puzzle_length, const char *solution_bytes, int solution_length)
     *}
     */
    public static MemorySegment verifier_create_from_bytes(MemorySegment puzzle_bytes, int puzzle_length, MemorySegment solution_bytes,
                                                           int solution_length) {
        var mh$ = verifier_create_from_bytes.HANDLE;
        try {
            return (MemorySegment) mh$.invokeExact(puzzle_bytes, puzzle_length, solution_bytes, solution_length);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_create_from_bytes_without_copying {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_POINTER, OmSimWrapper.C_POINTER,
                                                                            OmSimWrapper.C_INT, OmSimWrapper.C_POINTER, OmSimWrapper.C_INT);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_create_from_bytes_without_copying");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * void *verifier_create_from_bytes_without_copying(const char *puzzle_bytes, int puzzle_length, const char *solution_bytes, int solution_length)
     *}
     */
    public static FunctionDescriptor verifier_create_from_bytes_without_copying$descriptor() {
        return verifier_create_from_bytes_without_copying.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * void *verifier_create_from_bytes_without_copying(const char *puzzle_bytes, int puzzle_length, const char *solution_bytes, int solution_length)
     *}
     */
    public static MethodHandle verifier_create_from_bytes_without_copying$handle() {
        return verifier_create_from_bytes_without_copying.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * void *verifier_create_from_bytes_without_copying(const char *puzzle_bytes, int puzzle_length, const char *solution_bytes, int solution_length)
     *}
     */
    public static MemorySegment verifier_create_from_bytes_without_copying$address() {
        return verifier_create_from_bytes_without_copying.ADDR;
    }

    /**
     * {@snippet lang = c:
     * void *verifier_create_from_bytes_without_copying(const char *puzzle_bytes, int puzzle_length, const char *solution_bytes, int solution_length)
     *}
     */
    public static MemorySegment verifier_create_from_bytes_without_copying(MemorySegment puzzle_bytes, int puzzle_length,
                                                                           MemorySegment solution_bytes, int solution_length) {
        var mh$ = verifier_create_from_bytes_without_copying.HANDLE;
        try {
            return (MemorySegment) mh$.invokeExact(puzzle_bytes, puzzle_length, solution_bytes, solution_length);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_destroy {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_destroy");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * void verifier_destroy(void *verifier)
     *}
     */
    public static FunctionDescriptor verifier_destroy$descriptor() {
        return verifier_destroy.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * void verifier_destroy(void *verifier)
     *}
     */
    public static MethodHandle verifier_destroy$handle() {
        return verifier_destroy.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * void verifier_destroy(void *verifier)
     *}
     */
    public static MemorySegment verifier_destroy$address() {
        return verifier_destroy.ADDR;
    }

    /**
     * {@snippet lang = c:
     * void verifier_destroy(void *verifier)
     *}
     */
    public static void verifier_destroy(MemorySegment verifier) {
        var mh$ = verifier_destroy.HANDLE;
        try {
            mh$.invokeExact(verifier);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_set_cycle_limit {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(OmSimWrapper.C_POINTER, OmSimWrapper.C_INT);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_set_cycle_limit");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * void verifier_set_cycle_limit(void *verifier, int cycle_limit)
     *}
     */
    public static FunctionDescriptor verifier_set_cycle_limit$descriptor() {
        return verifier_set_cycle_limit.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * void verifier_set_cycle_limit(void *verifier, int cycle_limit)
     *}
     */
    public static MethodHandle verifier_set_cycle_limit$handle() {
        return verifier_set_cycle_limit.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * void verifier_set_cycle_limit(void *verifier, int cycle_limit)
     *}
     */
    public static MemorySegment verifier_set_cycle_limit$address() {
        return verifier_set_cycle_limit.ADDR;
    }

    /**
     * {@snippet lang = c:
     * void verifier_set_cycle_limit(void *verifier, int cycle_limit)
     *}
     */
    public static void verifier_set_cycle_limit(MemorySegment verifier, int cycle_limit) {
        var mh$ = verifier_set_cycle_limit.HANDLE;
        try {
            mh$.invokeExact(verifier, cycle_limit);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_disable_limits {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_disable_limits");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * void verifier_disable_limits(void *verifier)
     *}
     */
    public static FunctionDescriptor verifier_disable_limits$descriptor() {
        return verifier_disable_limits.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * void verifier_disable_limits(void *verifier)
     *}
     */
    public static MethodHandle verifier_disable_limits$handle() {
        return verifier_disable_limits.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * void verifier_disable_limits(void *verifier)
     *}
     */
    public static MemorySegment verifier_disable_limits$address() {
        return verifier_disable_limits.ADDR;
    }

    /**
     * {@snippet lang = c:
     * void verifier_disable_limits(void *verifier)
     *}
     */
    public static void verifier_disable_limits(MemorySegment verifier) {
        var mh$ = verifier_disable_limits.HANDLE;
        try {
            mh$.invokeExact(verifier);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_set_fails_on_wrong_output {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(OmSimWrapper.C_POINTER, OmSimWrapper.C_INT,
                                                                                OmSimWrapper.C_INT);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_set_fails_on_wrong_output");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * void verifier_set_fails_on_wrong_output(void *verifier, int output_index, int fails_on_wrong_output)
     *}
     */
    public static FunctionDescriptor verifier_set_fails_on_wrong_output$descriptor() {
        return verifier_set_fails_on_wrong_output.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * void verifier_set_fails_on_wrong_output(void *verifier, int output_index, int fails_on_wrong_output)
     *}
     */
    public static MethodHandle verifier_set_fails_on_wrong_output$handle() {
        return verifier_set_fails_on_wrong_output.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * void verifier_set_fails_on_wrong_output(void *verifier, int output_index, int fails_on_wrong_output)
     *}
     */
    public static MemorySegment verifier_set_fails_on_wrong_output$address() {
        return verifier_set_fails_on_wrong_output.ADDR;
    }

    /**
     * {@snippet lang = c:
     * void verifier_set_fails_on_wrong_output(void *verifier, int output_index, int fails_on_wrong_output)
     *}
     */
    public static void verifier_set_fails_on_wrong_output(MemorySegment verifier, int output_index, int fails_on_wrong_output) {
        var mh$ = verifier_set_fails_on_wrong_output.HANDLE;
        try {
            mh$.invokeExact(verifier, output_index, fails_on_wrong_output);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_set_fails_on_wrong_output_bonds {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(OmSimWrapper.C_POINTER, OmSimWrapper.C_INT,
                                                                                OmSimWrapper.C_INT);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_set_fails_on_wrong_output_bonds");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * void verifier_set_fails_on_wrong_output_bonds(void *verifier, int output_index, int fails_on_wrong_output_bonds)
     *}
     */
    public static FunctionDescriptor verifier_set_fails_on_wrong_output_bonds$descriptor() {
        return verifier_set_fails_on_wrong_output_bonds.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * void verifier_set_fails_on_wrong_output_bonds(void *verifier, int output_index, int fails_on_wrong_output_bonds)
     *}
     */
    public static MethodHandle verifier_set_fails_on_wrong_output_bonds$handle() {
        return verifier_set_fails_on_wrong_output_bonds.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * void verifier_set_fails_on_wrong_output_bonds(void *verifier, int output_index, int fails_on_wrong_output_bonds)
     *}
     */
    public static MemorySegment verifier_set_fails_on_wrong_output_bonds$address() {
        return verifier_set_fails_on_wrong_output_bonds.ADDR;
    }

    /**
     * {@snippet lang = c:
     * void verifier_set_fails_on_wrong_output_bonds(void *verifier, int output_index, int fails_on_wrong_output_bonds)
     *}
     */
    public static void verifier_set_fails_on_wrong_output_bonds(MemorySegment verifier, int output_index, int fails_on_wrong_output_bonds) {
        var mh$ = verifier_set_fails_on_wrong_output_bonds.HANDLE;
        try {
            mh$.invokeExact(verifier, output_index, fails_on_wrong_output_bonds);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_wrong_output_index {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_INT, OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_wrong_output_index");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * int verifier_wrong_output_index(void *verifier)
     *}
     */
    public static FunctionDescriptor verifier_wrong_output_index$descriptor() {
        return verifier_wrong_output_index.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * int verifier_wrong_output_index(void *verifier)
     *}
     */
    public static MethodHandle verifier_wrong_output_index$handle() {
        return verifier_wrong_output_index.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * int verifier_wrong_output_index(void *verifier)
     *}
     */
    public static MemorySegment verifier_wrong_output_index$address() {
        return verifier_wrong_output_index.ADDR;
    }

    /**
     * {@snippet lang = c:
     * int verifier_wrong_output_index(void *verifier)
     *}
     */
    public static int verifier_wrong_output_index(MemorySegment verifier) {
        var mh$ = verifier_wrong_output_index.HANDLE;
        try {
            return (int) mh$.invokeExact(verifier);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_wrong_output_atom {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_INT, OmSimWrapper.C_POINTER, OmSimWrapper.C_INT,
                                                                            OmSimWrapper.C_INT);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_wrong_output_atom");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * int verifier_wrong_output_atom(void *verifier, int u, int v)
     *}
     */
    public static FunctionDescriptor verifier_wrong_output_atom$descriptor() {
        return verifier_wrong_output_atom.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * int verifier_wrong_output_atom(void *verifier, int u, int v)
     *}
     */
    public static MethodHandle verifier_wrong_output_atom$handle() {
        return verifier_wrong_output_atom.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * int verifier_wrong_output_atom(void *verifier, int u, int v)
     *}
     */
    public static MemorySegment verifier_wrong_output_atom$address() {
        return verifier_wrong_output_atom.ADDR;
    }

    /**
     * {@snippet lang = c:
     * int verifier_wrong_output_atom(void *verifier, int u, int v)
     *}
     */
    public static int verifier_wrong_output_atom(MemorySegment verifier, int u, int v) {
        var mh$ = verifier_wrong_output_atom.HANDLE;
        try {
            return (int) mh$.invokeExact(verifier, u, v);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_wrong_output_clear {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_wrong_output_clear");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * void verifier_wrong_output_clear(void *verifier)
     *}
     */
    public static FunctionDescriptor verifier_wrong_output_clear$descriptor() {
        return verifier_wrong_output_clear.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * void verifier_wrong_output_clear(void *verifier)
     *}
     */
    public static MethodHandle verifier_wrong_output_clear$handle() {
        return verifier_wrong_output_clear.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * void verifier_wrong_output_clear(void *verifier)
     *}
     */
    public static MemorySegment verifier_wrong_output_clear$address() {
        return verifier_wrong_output_clear.ADDR;
    }

    /**
     * {@snippet lang = c:
     * void verifier_wrong_output_clear(void *verifier)
     *}
     */
    public static void verifier_wrong_output_clear(MemorySegment verifier) {
        var mh$ = verifier_wrong_output_clear.HANDLE;
        try {
            mh$.invokeExact(verifier);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_set_throughput_margin {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(OmSimWrapper.C_POINTER, OmSimWrapper.C_INT);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_set_throughput_margin");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * void verifier_set_throughput_margin(void *verifier, int margin)
     *}
     */
    public static FunctionDescriptor verifier_set_throughput_margin$descriptor() {
        return verifier_set_throughput_margin.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * void verifier_set_throughput_margin(void *verifier, int margin)
     *}
     */
    public static MethodHandle verifier_set_throughput_margin$handle() {
        return verifier_set_throughput_margin.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * void verifier_set_throughput_margin(void *verifier, int margin)
     *}
     */
    public static MemorySegment verifier_set_throughput_margin$address() {
        return verifier_set_throughput_margin.ADDR;
    }

    /**
     * {@snippet lang = c:
     * void verifier_set_throughput_margin(void *verifier, int margin)
     *}
     */
    public static void verifier_set_throughput_margin(MemorySegment verifier, int margin) {
        var mh$ = verifier_set_throughput_margin.HANDLE;
        try {
            mh$.invokeExact(verifier, margin);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_error {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_POINTER, OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_error");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * const char *verifier_error(void *verifier)
     *}
     */
    public static FunctionDescriptor verifier_error$descriptor() {
        return verifier_error.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * const char *verifier_error(void *verifier)
     *}
     */
    public static MethodHandle verifier_error$handle() {
        return verifier_error.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * const char *verifier_error(void *verifier)
     *}
     */
    public static MemorySegment verifier_error$address() {
        return verifier_error.ADDR;
    }

    /**
     * {@snippet lang = c:
     * const char *verifier_error(void *verifier)
     *}
     */
    public static MemorySegment verifier_error(MemorySegment verifier) {
        var mh$ = verifier_error.HANDLE;
        try {
            return (MemorySegment) mh$.invokeExact(verifier);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_error_cycle {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_INT, OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_error_cycle");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * int verifier_error_cycle(void *verifier)
     *}
     */
    public static FunctionDescriptor verifier_error_cycle$descriptor() {
        return verifier_error_cycle.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * int verifier_error_cycle(void *verifier)
     *}
     */
    public static MethodHandle verifier_error_cycle$handle() {
        return verifier_error_cycle.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * int verifier_error_cycle(void *verifier)
     *}
     */
    public static MemorySegment verifier_error_cycle$address() {
        return verifier_error_cycle.ADDR;
    }

    /**
     * {@snippet lang = c:
     * int verifier_error_cycle(void *verifier)
     *}
     */
    public static int verifier_error_cycle(MemorySegment verifier) {
        var mh$ = verifier_error_cycle.HANDLE;
        try {
            return (int) mh$.invokeExact(verifier);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_error_location_u {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_INT, OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_error_location_u");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * int verifier_error_location_u(void *verifier)
     *}
     */
    public static FunctionDescriptor verifier_error_location_u$descriptor() {
        return verifier_error_location_u.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * int verifier_error_location_u(void *verifier)
     *}
     */
    public static MethodHandle verifier_error_location_u$handle() {
        return verifier_error_location_u.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * int verifier_error_location_u(void *verifier)
     *}
     */
    public static MemorySegment verifier_error_location_u$address() {
        return verifier_error_location_u.ADDR;
    }

    /**
     * {@snippet lang = c:
     * int verifier_error_location_u(void *verifier)
     *}
     */
    public static int verifier_error_location_u(MemorySegment verifier) {
        var mh$ = verifier_error_location_u.HANDLE;
        try {
            return (int) mh$.invokeExact(verifier);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_error_location_v {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_INT, OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_error_location_v");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * int verifier_error_location_v(void *verifier)
     *}
     */
    public static FunctionDescriptor verifier_error_location_v$descriptor() {
        return verifier_error_location_v.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * int verifier_error_location_v(void *verifier)
     *}
     */
    public static MethodHandle verifier_error_location_v$handle() {
        return verifier_error_location_v.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * int verifier_error_location_v(void *verifier)
     *}
     */
    public static MemorySegment verifier_error_location_v$address() {
        return verifier_error_location_v.ADDR;
    }

    /**
     * {@snippet lang = c:
     * int verifier_error_location_v(void *verifier)
     *}
     */
    public static int verifier_error_location_v(MemorySegment verifier) {
        var mh$ = verifier_error_location_v.HANDLE;
        try {
            return (int) mh$.invokeExact(verifier);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_error_clear {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_error_clear");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * void verifier_error_clear(void *verifier)
     *}
     */
    public static FunctionDescriptor verifier_error_clear$descriptor() {
        return verifier_error_clear.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * void verifier_error_clear(void *verifier)
     *}
     */
    public static MethodHandle verifier_error_clear$handle() {
        return verifier_error_clear.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * void verifier_error_clear(void *verifier)
     *}
     */
    public static MemorySegment verifier_error_clear$address() {
        return verifier_error_clear.ADDR;
    }

    /**
     * {@snippet lang = c:
     * void verifier_error_clear(void *verifier)
     *}
     */
    public static void verifier_error_clear(MemorySegment verifier) {
        var mh$ = verifier_error_clear.HANDLE;
        try {
            mh$.invokeExact(verifier);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_evaluate_metric {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_INT, OmSimWrapper.C_POINTER,
                                                                            OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_evaluate_metric");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * int verifier_evaluate_metric(void *verifier, const char *metric)
     *}
     */
    public static FunctionDescriptor verifier_evaluate_metric$descriptor() {
        return verifier_evaluate_metric.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * int verifier_evaluate_metric(void *verifier, const char *metric)
     *}
     */
    public static MethodHandle verifier_evaluate_metric$handle() {
        return verifier_evaluate_metric.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * int verifier_evaluate_metric(void *verifier, const char *metric)
     *}
     */
    public static MemorySegment verifier_evaluate_metric$address() {
        return verifier_evaluate_metric.ADDR;
    }

    /**
     * {@snippet lang = c:
     * int verifier_evaluate_metric(void *verifier, const char *metric)
     *}
     */
    public static int verifier_evaluate_metric(MemorySegment verifier, MemorySegment metric) {
        var mh$ = verifier_evaluate_metric.HANDLE;
        try {
            return (int) mh$.invokeExact(verifier, metric);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_evaluate_approximate_metric {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_DOUBLE, OmSimWrapper.C_POINTER,
                                                                            OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_evaluate_approximate_metric");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * double verifier_evaluate_approximate_metric(void *verifier, const char *metric)
     *}
     */
    public static FunctionDescriptor verifier_evaluate_approximate_metric$descriptor() {
        return verifier_evaluate_approximate_metric.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * double verifier_evaluate_approximate_metric(void *verifier, const char *metric)
     *}
     */
    public static MethodHandle verifier_evaluate_approximate_metric$handle() {
        return verifier_evaluate_approximate_metric.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * double verifier_evaluate_approximate_metric(void *verifier, const char *metric)
     *}
     */
    public static MemorySegment verifier_evaluate_approximate_metric$address() {
        return verifier_evaluate_approximate_metric.ADDR;
    }

    /**
     * {@snippet lang = c:
     * double verifier_evaluate_approximate_metric(void *verifier, const char *metric)
     *}
     */
    public static double verifier_evaluate_approximate_metric(MemorySegment verifier, MemorySegment metric) {
        var mh$ = verifier_evaluate_approximate_metric.HANDLE;
        try {
            return (double) mh$.invokeExact(verifier, metric);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_number_of_output_intervals {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_INT, OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_number_of_output_intervals");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * int verifier_number_of_output_intervals(void *verifier)
     *}
     */
    public static FunctionDescriptor verifier_number_of_output_intervals$descriptor() {
        return verifier_number_of_output_intervals.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * int verifier_number_of_output_intervals(void *verifier)
     *}
     */
    public static MethodHandle verifier_number_of_output_intervals$handle() {
        return verifier_number_of_output_intervals.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * int verifier_number_of_output_intervals(void *verifier)
     *}
     */
    public static MemorySegment verifier_number_of_output_intervals$address() {
        return verifier_number_of_output_intervals.ADDR;
    }

    /**
     * {@snippet lang = c:
     * int verifier_number_of_output_intervals(void *verifier)
     *}
     */
    public static int verifier_number_of_output_intervals(MemorySegment verifier) {
        var mh$ = verifier_number_of_output_intervals.HANDLE;
        try {
            return (int) mh$.invokeExact(verifier);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_output_interval {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_INT, OmSimWrapper.C_POINTER, OmSimWrapper.C_INT);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_output_interval");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * int verifier_output_interval(void *verifier, int which_interval)
     *}
     */
    public static FunctionDescriptor verifier_output_interval$descriptor() {
        return verifier_output_interval.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * int verifier_output_interval(void *verifier, int which_interval)
     *}
     */
    public static MethodHandle verifier_output_interval$handle() {
        return verifier_output_interval.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * int verifier_output_interval(void *verifier, int which_interval)
     *}
     */
    public static MemorySegment verifier_output_interval$address() {
        return verifier_output_interval.ADDR;
    }

    /**
     * {@snippet lang = c:
     * int verifier_output_interval(void *verifier, int which_interval)
     *}
     */
    public static int verifier_output_interval(MemorySegment verifier, int which_interval) {
        var mh$ = verifier_output_interval.HANDLE;
        try {
            return (int) mh$.invokeExact(verifier, which_interval);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class verifier_output_intervals_repeat_after {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(OmSimWrapper.C_INT, OmSimWrapper.C_POINTER);

        public static final MemorySegment ADDR = OmSimWrapper.findOrThrow("verifier_output_intervals_repeat_after");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang = c:
     * int verifier_output_intervals_repeat_after(void *verifier)
     *}
     */
    public static FunctionDescriptor verifier_output_intervals_repeat_after$descriptor() {
        return verifier_output_intervals_repeat_after.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang = c:
     * int verifier_output_intervals_repeat_after(void *verifier)
     *}
     */
    public static MethodHandle verifier_output_intervals_repeat_after$handle() {
        return verifier_output_intervals_repeat_after.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang = c:
     * int verifier_output_intervals_repeat_after(void *verifier)
     *}
     */
    public static MemorySegment verifier_output_intervals_repeat_after$address() {
        return verifier_output_intervals_repeat_after.ADDR;
    }

    /**
     * {@snippet lang = c:
     * int verifier_output_intervals_repeat_after(void *verifier)
     *}
     */
    public static int verifier_output_intervals_repeat_after(MemorySegment verifier) {
        var mh$ = verifier_output_intervals_repeat_after.HANDLE;
        try {
            return (int) mh$.invokeExact(verifier);
        }
        catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
}
