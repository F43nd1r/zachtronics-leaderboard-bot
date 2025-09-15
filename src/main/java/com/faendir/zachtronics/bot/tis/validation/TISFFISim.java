package com.faendir.zachtronics.bot.tis.validation;

import com.faendir.zachtronics.bot.validation.NativeLoader;

import java.lang.invoke.*;
import java.lang.foreign.*;
import java.util.*;
import java.util.stream.*;

import static java.lang.foreign.ValueLayout.*;

/**
 * TIS-100-CXX FFM wrapper, regenerate if TIS-100-CXX changes its public API via:
 * * download jextract from <a href="https://jdk.java.net/jextract/">here</a>
 * * run `./jextract -D TIS_ENABLE_LUA TIS-100-CXX/tis100.h`
 * * replace the default `SYMBOL_LOOKUP` with `NativeLoader.loadLibrary("TIS100", LIBRARY_ARENA);`
 * * remove the unused downcall tracer (optional)
 * * add `@SuppressWarnings("unused")` (optional)
 */
@SuppressWarnings("unused")
public class TISFFISim {

    TISFFISim() {
        // Should not be called directly
    }

    static final Arena LIBRARY_ARENA = Arena.ofAuto();
    static final SymbolLookup SYMBOL_LOOKUP = NativeLoader.loadLibrary("TIS100", LIBRARY_ARENA);

    static void traceDowncall(String name, Object... args) {
         String traceArgs = Arrays.stream(args)
                       .map(Object::toString)
                       .collect(Collectors.joining(", "));
         System.out.printf("%s(%s)\n", name, traceArgs);
    }

    static MemorySegment findOrThrow(String symbol) {
        return SYMBOL_LOOKUP.find(symbol)
            .orElseThrow(() -> new UnsatisfiedLinkError("unresolved symbol: " + symbol));
    }

    static MethodHandle upcallHandle(Class<?> fi, String name, FunctionDescriptor fdesc) {
        try {
            return MethodHandles.lookup().findVirtual(fi, name, fdesc.toMethodType());
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    static MemoryLayout align(MemoryLayout layout, long align) {
        return switch (layout) {
            case PaddingLayout p -> p;
            case ValueLayout v -> v.withByteAlignment(align);
            case GroupLayout g -> {
                MemoryLayout[] alignedMembers = g.memberLayouts().stream()
                        .map(m -> align(m, align)).toArray(MemoryLayout[]::new);
                yield g instanceof StructLayout ?
                        MemoryLayout.structLayout(alignedMembers) : MemoryLayout.unionLayout(alignedMembers);
            }
            case SequenceLayout s -> MemoryLayout.sequenceLayout(s.elementCount(), align(s.elementLayout(), align));
        };
    }

    public static final ValueLayout.OfBoolean C_BOOL = ValueLayout.JAVA_BOOLEAN;
    public static final ValueLayout.OfByte C_CHAR = ValueLayout.JAVA_BYTE;
    public static final ValueLayout.OfShort C_SHORT = ValueLayout.JAVA_SHORT;
    public static final ValueLayout.OfInt C_INT = ValueLayout.JAVA_INT;
    public static final ValueLayout.OfFloat C_FLOAT = ValueLayout.JAVA_FLOAT;
    public static final ValueLayout.OfDouble C_DOUBLE = ValueLayout.JAVA_DOUBLE;
    public static final AddressLayout C_POINTER = ValueLayout.ADDRESS
            .withTargetLayout(MemoryLayout.sequenceLayout(java.lang.Long.MAX_VALUE, JAVA_BYTE));
    public static final ValueLayout.OfLong C_LONG = ValueLayout.JAVA_LONG;

    private static class tis_sim_create {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            TISFFISim.C_POINTER    );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_create");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * struct tis_sim *tis_sim_create()
     * }
     */
    public static FunctionDescriptor tis_sim_create$descriptor() {
        return tis_sim_create.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * struct tis_sim *tis_sim_create()
     * }
     */
    public static MethodHandle tis_sim_create$handle() {
        return tis_sim_create.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * struct tis_sim *tis_sim_create()
     * }
     */
    public static MemorySegment tis_sim_create$address() {
        return tis_sim_create.ADDR;
    }

    /**
     * {@snippet lang=c :
     * struct tis_sim *tis_sim_create()
     * }
     */
    public static MemorySegment tis_sim_create() {
        var mh$ = tis_sim_create.HANDLE;
        try {
            return (MemorySegment)mh$.invokeExact();
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_destroy {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            TISFFISim.C_POINTER
        );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_destroy");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * void tis_sim_destroy(struct tis_sim *sim)
     * }
     */
    public static FunctionDescriptor tis_sim_destroy$descriptor() {
        return tis_sim_destroy.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * void tis_sim_destroy(struct tis_sim *sim)
     * }
     */
    public static MethodHandle tis_sim_destroy$handle() {
        return tis_sim_destroy.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * void tis_sim_destroy(struct tis_sim *sim)
     * }
     */
    public static MemorySegment tis_sim_destroy$address() {
        return tis_sim_destroy.ADDR;
    }

    /**
     * {@snippet lang=c :
     * void tis_sim_destroy(struct tis_sim *sim)
     * }
     */
    public static void tis_sim_destroy(MemorySegment sim) {
        var mh$ = tis_sim_destroy.HANDLE;
        try {
            mh$.invokeExact(sim);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_add_seed_range {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            TISFFISim.C_POINTER,
            TISFFISim.C_INT,
            TISFFISim.C_INT
        );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_add_seed_range");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * void tis_sim_add_seed_range(struct tis_sim *sim, uint32_t begin, uint32_t end)
     * }
     */
    public static FunctionDescriptor tis_sim_add_seed_range$descriptor() {
        return tis_sim_add_seed_range.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * void tis_sim_add_seed_range(struct tis_sim *sim, uint32_t begin, uint32_t end)
     * }
     */
    public static MethodHandle tis_sim_add_seed_range$handle() {
        return tis_sim_add_seed_range.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * void tis_sim_add_seed_range(struct tis_sim *sim, uint32_t begin, uint32_t end)
     * }
     */
    public static MemorySegment tis_sim_add_seed_range$address() {
        return tis_sim_add_seed_range.ADDR;
    }

    /**
     * {@snippet lang=c :
     * void tis_sim_add_seed_range(struct tis_sim *sim, uint32_t begin, uint32_t end)
     * }
     */
    public static void tis_sim_add_seed_range(MemorySegment sim, int begin, int end) {
        var mh$ = tis_sim_add_seed_range.HANDLE;
        try {
            mh$.invokeExact(sim, begin, end);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_set_builtin_level_name {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            TISFFISim.C_POINTER,
            TISFFISim.C_POINTER
        );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_set_builtin_level_name");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * void tis_sim_set_builtin_level_name(struct tis_sim *sim, const char *builtin_level_name)
     * }
     */
    public static FunctionDescriptor tis_sim_set_builtin_level_name$descriptor() {
        return tis_sim_set_builtin_level_name.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * void tis_sim_set_builtin_level_name(struct tis_sim *sim, const char *builtin_level_name)
     * }
     */
    public static MethodHandle tis_sim_set_builtin_level_name$handle() {
        return tis_sim_set_builtin_level_name.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * void tis_sim_set_builtin_level_name(struct tis_sim *sim, const char *builtin_level_name)
     * }
     */
    public static MemorySegment tis_sim_set_builtin_level_name$address() {
        return tis_sim_set_builtin_level_name.ADDR;
    }

    /**
     * {@snippet lang=c :
     * void tis_sim_set_builtin_level_name(struct tis_sim *sim, const char *builtin_level_name)
     * }
     */
    public static void tis_sim_set_builtin_level_name(MemorySegment sim, MemorySegment builtin_level_name) {
        var mh$ = tis_sim_set_builtin_level_name.HANDLE;
        try {
            mh$.invokeExact(sim, builtin_level_name);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_set_custom_spec_path {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            TISFFISim.C_POINTER,
            TISFFISim.C_POINTER
                                                                               );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_set_custom_spec_path");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * void tis_sim_set_custom_spec_path(struct tis_sim *sim, const char *custom_spec_path)
     * }
     */
    public static FunctionDescriptor tis_sim_set_custom_spec_path$descriptor() {
        return tis_sim_set_custom_spec_path.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * void tis_sim_set_custom_spec_path(struct tis_sim *sim, const char *custom_spec_path)
     * }
     */
    public static MethodHandle tis_sim_set_custom_spec_path$handle() {
        return tis_sim_set_custom_spec_path.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * void tis_sim_set_custom_spec_path(struct tis_sim *sim, const char *custom_spec_path)
     * }
     */
    public static MemorySegment tis_sim_set_custom_spec_path$address() {
        return tis_sim_set_custom_spec_path.ADDR;
    }

    /**
     * {@snippet lang=c :
     * void tis_sim_set_custom_spec_path(struct tis_sim *sim, const char *custom_spec_path)
     * }
     */
    public static void tis_sim_set_custom_spec_path(MemorySegment sim, MemorySegment custom_spec_path) {
        var mh$ = tis_sim_set_custom_spec_path.HANDLE;
        try {
            mh$.invokeExact(sim, custom_spec_path);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_set_custom_spec_code {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            TISFFISim.C_POINTER,
            TISFFISim.C_POINTER,
            TISFFISim.C_INT
                                                                               );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_set_custom_spec_code");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * void tis_sim_set_custom_spec_code(struct tis_sim *sim, const char *custom_spec_code, uint32_t base_seed)
     * }
     */
    public static FunctionDescriptor tis_sim_set_custom_spec_code$descriptor() {
        return tis_sim_set_custom_spec_code.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * void tis_sim_set_custom_spec_code(struct tis_sim *sim, const char *custom_spec_code, uint32_t base_seed)
     * }
     */
    public static MethodHandle tis_sim_set_custom_spec_code$handle() {
        return tis_sim_set_custom_spec_code.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * void tis_sim_set_custom_spec_code(struct tis_sim *sim, const char *custom_spec_code, uint32_t base_seed)
     * }
     */
    public static MemorySegment tis_sim_set_custom_spec_code$address() {
        return tis_sim_set_custom_spec_code.ADDR;
    }

    /**
     * {@snippet lang=c :
     * void tis_sim_set_custom_spec_code(struct tis_sim *sim, const char *custom_spec_code, uint32_t base_seed)
     * }
     */
    public static void tis_sim_set_custom_spec_code(MemorySegment sim, MemorySegment custom_spec_code, int base_seed) {
        var mh$ = tis_sim_set_custom_spec_code.HANDLE;
        try {
            mh$.invokeExact(sim, custom_spec_code, base_seed);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_set_num_threads {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            TISFFISim.C_POINTER,
            TISFFISim.C_INT
        );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_set_num_threads");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * void tis_sim_set_num_threads(struct tis_sim *sim, uint32_t num_threads)
     * }
     */
    public static FunctionDescriptor tis_sim_set_num_threads$descriptor() {
        return tis_sim_set_num_threads.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * void tis_sim_set_num_threads(struct tis_sim *sim, uint32_t num_threads)
     * }
     */
    public static MethodHandle tis_sim_set_num_threads$handle() {
        return tis_sim_set_num_threads.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * void tis_sim_set_num_threads(struct tis_sim *sim, uint32_t num_threads)
     * }
     */
    public static MemorySegment tis_sim_set_num_threads$address() {
        return tis_sim_set_num_threads.ADDR;
    }

    /**
     * {@snippet lang=c :
     * void tis_sim_set_num_threads(struct tis_sim *sim, uint32_t num_threads)
     * }
     */
    public static void tis_sim_set_num_threads(MemorySegment sim, int num_threads) {
        var mh$ = tis_sim_set_num_threads.HANDLE;
        try {
            mh$.invokeExact(sim, num_threads);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_set_cycles_limit {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            TISFFISim.C_POINTER,
            TISFFISim.C_LONG
        );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_set_cycles_limit");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * void tis_sim_set_cycles_limit(struct tis_sim *sim, size_t cycles_limit)
     * }
     */
    public static FunctionDescriptor tis_sim_set_cycles_limit$descriptor() {
        return tis_sim_set_cycles_limit.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * void tis_sim_set_cycles_limit(struct tis_sim *sim, size_t cycles_limit)
     * }
     */
    public static MethodHandle tis_sim_set_cycles_limit$handle() {
        return tis_sim_set_cycles_limit.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * void tis_sim_set_cycles_limit(struct tis_sim *sim, size_t cycles_limit)
     * }
     */
    public static MemorySegment tis_sim_set_cycles_limit$address() {
        return tis_sim_set_cycles_limit.ADDR;
    }

    /**
     * {@snippet lang=c :
     * void tis_sim_set_cycles_limit(struct tis_sim *sim, size_t cycles_limit)
     * }
     */
    public static void tis_sim_set_cycles_limit(MemorySegment sim, long cycles_limit) {
        var mh$ = tis_sim_set_cycles_limit.HANDLE;
        try {
            mh$.invokeExact(sim, cycles_limit);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_set_total_cycles_limit {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            TISFFISim.C_POINTER,
            TISFFISim.C_LONG
        );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_set_total_cycles_limit");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * void tis_sim_set_total_cycles_limit(struct tis_sim *sim, size_t total_cycles_limit)
     * }
     */
    public static FunctionDescriptor tis_sim_set_total_cycles_limit$descriptor() {
        return tis_sim_set_total_cycles_limit.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * void tis_sim_set_total_cycles_limit(struct tis_sim *sim, size_t total_cycles_limit)
     * }
     */
    public static MethodHandle tis_sim_set_total_cycles_limit$handle() {
        return tis_sim_set_total_cycles_limit.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * void tis_sim_set_total_cycles_limit(struct tis_sim *sim, size_t total_cycles_limit)
     * }
     */
    public static MemorySegment tis_sim_set_total_cycles_limit$address() {
        return tis_sim_set_total_cycles_limit.ADDR;
    }

    /**
     * {@snippet lang=c :
     * void tis_sim_set_total_cycles_limit(struct tis_sim *sim, size_t total_cycles_limit)
     * }
     */
    public static void tis_sim_set_total_cycles_limit(MemorySegment sim, long total_cycles_limit) {
        var mh$ = tis_sim_set_total_cycles_limit.HANDLE;
        try {
            mh$.invokeExact(sim, total_cycles_limit);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_set_cheat_rate {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            TISFFISim.C_POINTER,
            TISFFISim.C_DOUBLE
        );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_set_cheat_rate");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * void tis_sim_set_cheat_rate(struct tis_sim *sim, double cheat_rate)
     * }
     */
    public static FunctionDescriptor tis_sim_set_cheat_rate$descriptor() {
        return tis_sim_set_cheat_rate.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * void tis_sim_set_cheat_rate(struct tis_sim *sim, double cheat_rate)
     * }
     */
    public static MethodHandle tis_sim_set_cheat_rate$handle() {
        return tis_sim_set_cheat_rate.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * void tis_sim_set_cheat_rate(struct tis_sim *sim, double cheat_rate)
     * }
     */
    public static MemorySegment tis_sim_set_cheat_rate$address() {
        return tis_sim_set_cheat_rate.ADDR;
    }

    /**
     * {@snippet lang=c :
     * void tis_sim_set_cheat_rate(struct tis_sim *sim, double cheat_rate)
     * }
     */
    public static void tis_sim_set_cheat_rate(MemorySegment sim, double cheat_rate) {
        var mh$ = tis_sim_set_cheat_rate.HANDLE;
        try {
            mh$.invokeExact(sim, cheat_rate);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_set_limit_multiplier {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            TISFFISim.C_POINTER,
            TISFFISim.C_DOUBLE
        );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_set_limit_multiplier");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * void tis_sim_set_limit_multiplier(struct tis_sim *sim, double limit_multiplier)
     * }
     */
    public static FunctionDescriptor tis_sim_set_limit_multiplier$descriptor() {
        return tis_sim_set_limit_multiplier.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * void tis_sim_set_limit_multiplier(struct tis_sim *sim, double limit_multiplier)
     * }
     */
    public static MethodHandle tis_sim_set_limit_multiplier$handle() {
        return tis_sim_set_limit_multiplier.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * void tis_sim_set_limit_multiplier(struct tis_sim *sim, double limit_multiplier)
     * }
     */
    public static MemorySegment tis_sim_set_limit_multiplier$address() {
        return tis_sim_set_limit_multiplier.ADDR;
    }

    /**
     * {@snippet lang=c :
     * void tis_sim_set_limit_multiplier(struct tis_sim *sim, double limit_multiplier)
     * }
     */
    public static void tis_sim_set_limit_multiplier(MemorySegment sim, double limit_multiplier) {
        var mh$ = tis_sim_set_limit_multiplier.HANDLE;
        try {
            mh$.invokeExact(sim, limit_multiplier);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_set_T21_size {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            TISFFISim.C_POINTER,
            TISFFISim.C_INT
        );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_set_T21_size");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * void tis_sim_set_T21_size(struct tis_sim *sim, uint32_t T21_size)
     * }
     */
    public static FunctionDescriptor tis_sim_set_T21_size$descriptor() {
        return tis_sim_set_T21_size.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * void tis_sim_set_T21_size(struct tis_sim *sim, uint32_t T21_size)
     * }
     */
    public static MethodHandle tis_sim_set_T21_size$handle() {
        return tis_sim_set_T21_size.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * void tis_sim_set_T21_size(struct tis_sim *sim, uint32_t T21_size)
     * }
     */
    public static MemorySegment tis_sim_set_T21_size$address() {
        return tis_sim_set_T21_size.ADDR;
    }

    /**
     * {@snippet lang=c :
     * void tis_sim_set_T21_size(struct tis_sim *sim, uint32_t T21_size)
     * }
     */
    public static void tis_sim_set_T21_size(MemorySegment sim, int T21_size) {
        var mh$ = tis_sim_set_T21_size.HANDLE;
        try {
            mh$.invokeExact(sim, T21_size);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_set_T30_size {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            TISFFISim.C_POINTER,
            TISFFISim.C_INT
        );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_set_T30_size");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * void tis_sim_set_T30_size(struct tis_sim *sim, uint32_t T30_size)
     * }
     */
    public static FunctionDescriptor tis_sim_set_T30_size$descriptor() {
        return tis_sim_set_T30_size.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * void tis_sim_set_T30_size(struct tis_sim *sim, uint32_t T30_size)
     * }
     */
    public static MethodHandle tis_sim_set_T30_size$handle() {
        return tis_sim_set_T30_size.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * void tis_sim_set_T30_size(struct tis_sim *sim, uint32_t T30_size)
     * }
     */
    public static MemorySegment tis_sim_set_T30_size$address() {
        return tis_sim_set_T30_size.ADDR;
    }

    /**
     * {@snippet lang=c :
     * void tis_sim_set_T30_size(struct tis_sim *sim, uint32_t T30_size)
     * }
     */
    public static void tis_sim_set_T30_size(MemorySegment sim, int T30_size) {
        var mh$ = tis_sim_set_T30_size.HANDLE;
        try {
            mh$.invokeExact(sim, T30_size);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_set_run_fixed {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            TISFFISim.C_POINTER,
            TISFFISim.C_BOOL
        );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_set_run_fixed");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * void tis_sim_set_run_fixed(struct tis_sim *sim, bool run_fixed)
     * }
     */
    public static FunctionDescriptor tis_sim_set_run_fixed$descriptor() {
        return tis_sim_set_run_fixed.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * void tis_sim_set_run_fixed(struct tis_sim *sim, bool run_fixed)
     * }
     */
    public static MethodHandle tis_sim_set_run_fixed$handle() {
        return tis_sim_set_run_fixed.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * void tis_sim_set_run_fixed(struct tis_sim *sim, bool run_fixed)
     * }
     */
    public static MemorySegment tis_sim_set_run_fixed$address() {
        return tis_sim_set_run_fixed.ADDR;
    }

    /**
     * {@snippet lang=c :
     * void tis_sim_set_run_fixed(struct tis_sim *sim, bool run_fixed)
     * }
     */
    public static void tis_sim_set_run_fixed(MemorySegment sim, boolean run_fixed) {
        var mh$ = tis_sim_set_run_fixed.HANDLE;
        try {
            mh$.invokeExact(sim, run_fixed);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_set_compute_stats {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            TISFFISim.C_POINTER,
            TISFFISim.C_BOOL
        );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_set_compute_stats");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * void tis_sim_set_compute_stats(struct tis_sim *sim, bool compute_stats)
     * }
     */
    public static FunctionDescriptor tis_sim_set_compute_stats$descriptor() {
        return tis_sim_set_compute_stats.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * void tis_sim_set_compute_stats(struct tis_sim *sim, bool compute_stats)
     * }
     */
    public static MethodHandle tis_sim_set_compute_stats$handle() {
        return tis_sim_set_compute_stats.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * void tis_sim_set_compute_stats(struct tis_sim *sim, bool compute_stats)
     * }
     */
    public static MemorySegment tis_sim_set_compute_stats$address() {
        return tis_sim_set_compute_stats.ADDR;
    }

    /**
     * {@snippet lang=c :
     * void tis_sim_set_compute_stats(struct tis_sim *sim, bool compute_stats)
     * }
     */
    public static void tis_sim_set_compute_stats(MemorySegment sim, boolean compute_stats) {
        var mh$ = tis_sim_set_compute_stats.HANDLE;
        try {
            mh$.invokeExact(sim, compute_stats);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_get_error_message {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            TISFFISim.C_POINTER,
            TISFFISim.C_POINTER
                                                                           );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_get_error_message");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * const char *tis_sim_get_error_message(const struct tis_sim *sim)
     * }
     */
    public static FunctionDescriptor tis_sim_get_error_message$descriptor() {
        return tis_sim_get_error_message.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * const char *tis_sim_get_error_message(const struct tis_sim *sim)
     * }
     */
    public static MethodHandle tis_sim_get_error_message$handle() {
        return tis_sim_get_error_message.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * const char *tis_sim_get_error_message(const struct tis_sim *sim)
     * }
     */
    public static MemorySegment tis_sim_get_error_message$address() {
        return tis_sim_get_error_message.ADDR;
    }

    /**
     * {@snippet lang=c :
     * const char *tis_sim_get_error_message(const struct tis_sim *sim)
     * }
     */
    public static MemorySegment tis_sim_get_error_message(MemorySegment sim) {
        var mh$ = tis_sim_get_error_message.HANDLE;
        try {
            return (MemorySegment)mh$.invokeExact(sim);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class tis_sim_simulate {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            TISFFISim.C_POINTER,
            TISFFISim.C_POINTER,
            TISFFISim.C_POINTER
                                                                           );

        public static final MemorySegment ADDR = TISFFISim.findOrThrow("tis_sim_simulate");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Function descriptor for:
     * {@snippet lang=c :
     * const struct score *tis_sim_simulate(struct tis_sim *sim, const char *code)
     * }
     */
    public static FunctionDescriptor tis_sim_simulate$descriptor() {
        return tis_sim_simulate.DESC;
    }

    /**
     * Downcall method handle for:
     * {@snippet lang=c :
     * const struct score *tis_sim_simulate(struct tis_sim *sim, const char *code)
     * }
     */
    public static MethodHandle tis_sim_simulate$handle() {
        return tis_sim_simulate.HANDLE;
    }

    /**
     * Address for:
     * {@snippet lang=c :
     * const struct score *tis_sim_simulate(struct tis_sim *sim, const char *code)
     * }
     */
    public static MemorySegment tis_sim_simulate$address() {
        return tis_sim_simulate.ADDR;
    }

    /**
     * {@snippet lang=c :
     * const struct score *tis_sim_simulate(struct tis_sim *sim, const char *code)
     * }
     */
    public static MemorySegment tis_sim_simulate(MemorySegment sim, MemorySegment code) {
        var mh$ = tis_sim_simulate.HANDLE;
        try {
            return (MemorySegment)mh$.invokeExact(sim, code);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
}
