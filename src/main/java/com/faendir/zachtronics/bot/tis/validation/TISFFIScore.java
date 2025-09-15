package com.faendir.zachtronics.bot.tis.validation;

import java.lang.foreign.*;
import java.util.function.Consumer;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.*;

/**
 * {@snippet lang = c:
 * struct score {
 *     size_t cycles;
 *     size_t nodes;
 *     size_t instructions;
 *     unsigned int random_test_ran;
 *     unsigned int random_test_valid;
 *     bool validated;
 *     bool achievement;
 *     bool cheat;
 *     bool hardcoded;
 * }
 *}
 */
@SuppressWarnings("unused")
public class TISFFIScore {

    TISFFIScore() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        TISFFISim.C_LONG.withName("cycles"),
        TISFFISim.C_LONG.withName("nodes"),
        TISFFISim.C_LONG.withName("instructions"),
        TISFFISim.C_INT.withName("random_test_ran"),
        TISFFISim.C_INT.withName("random_test_valid"),
        TISFFISim.C_BOOL.withName("validated"),
        TISFFISim.C_BOOL.withName("achievement"),
        TISFFISim.C_BOOL.withName("cheat"),
        TISFFISim.C_BOOL.withName("hardcoded"),
        MemoryLayout.paddingLayout(4)
                                                                        ).withName("score");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfLong cycles$LAYOUT = (OfLong) $LAYOUT.select(groupElement("cycles"));

    /**
     * Layout for field:
     * {@snippet lang = c:
     * size_t cycles
     *}
     */
    public static final OfLong cycles$layout() {
        return cycles$LAYOUT;
    }

    private static final long cycles$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang = c:
     * size_t cycles
     *}
     */
    public static final long cycles$offset() {
        return cycles$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang = c:
     * size_t cycles
     *}
     */
    public static long cycles(MemorySegment struct) {
        return struct.get(cycles$LAYOUT, cycles$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang = c:
     * size_t cycles
     *}
     */
    public static void cycles(MemorySegment struct, long fieldValue) {
        struct.set(cycles$LAYOUT, cycles$OFFSET, fieldValue);
    }

    private static final OfLong nodes$LAYOUT = (OfLong) $LAYOUT.select(groupElement("nodes"));

    /**
     * Layout for field:
     * {@snippet lang = c:
     * size_t nodes
     *}
     */
    public static final OfLong nodes$layout() {
        return nodes$LAYOUT;
    }

    private static final long nodes$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang = c:
     * size_t nodes
     *}
     */
    public static final long nodes$offset() {
        return nodes$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang = c:
     * size_t nodes
     *}
     */
    public static long nodes(MemorySegment struct) {
        return struct.get(nodes$LAYOUT, nodes$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang = c:
     * size_t nodes
     *}
     */
    public static void nodes(MemorySegment struct, long fieldValue) {
        struct.set(nodes$LAYOUT, nodes$OFFSET, fieldValue);
    }

    private static final OfLong instructions$LAYOUT = (OfLong) $LAYOUT.select(groupElement("instructions"));

    /**
     * Layout for field:
     * {@snippet lang = c:
     * size_t instructions
     *}
     */
    public static final OfLong instructions$layout() {
        return instructions$LAYOUT;
    }

    private static final long instructions$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang = c:
     * size_t instructions
     *}
     */
    public static final long instructions$offset() {
        return instructions$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang = c:
     * size_t instructions
     *}
     */
    public static long instructions(MemorySegment struct) {
        return struct.get(instructions$LAYOUT, instructions$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang = c:
     * size_t instructions
     *}
     */
    public static void instructions(MemorySegment struct, long fieldValue) {
        struct.set(instructions$LAYOUT, instructions$OFFSET, fieldValue);
    }

    private static final OfInt random_test_ran$LAYOUT = (OfInt) $LAYOUT.select(groupElement("random_test_ran"));

    /**
     * Layout for field:
     * {@snippet lang = c:
     * unsigned int random_test_ran
     *}
     */
    public static final OfInt random_test_ran$layout() {
        return random_test_ran$LAYOUT;
    }

    private static final long random_test_ran$OFFSET = 24;

    /**
     * Offset for field:
     * {@snippet lang = c:
     * unsigned int random_test_ran
     *}
     */
    public static final long random_test_ran$offset() {
        return random_test_ran$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang = c:
     * unsigned int random_test_ran
     *}
     */
    public static int random_test_ran(MemorySegment struct) {
        return struct.get(random_test_ran$LAYOUT, random_test_ran$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang = c:
     * unsigned int random_test_ran
     *}
     */
    public static void random_test_ran(MemorySegment struct, int fieldValue) {
        struct.set(random_test_ran$LAYOUT, random_test_ran$OFFSET, fieldValue);
    }

    private static final OfInt random_test_valid$LAYOUT = (OfInt) $LAYOUT.select(groupElement("random_test_valid"));

    /**
     * Layout for field:
     * {@snippet lang = c:
     * unsigned int random_test_valid
     *}
     */
    public static final OfInt random_test_valid$layout() {
        return random_test_valid$LAYOUT;
    }

    private static final long random_test_valid$OFFSET = 28;

    /**
     * Offset for field:
     * {@snippet lang = c:
     * unsigned int random_test_valid
     *}
     */
    public static final long random_test_valid$offset() {
        return random_test_valid$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang = c:
     * unsigned int random_test_valid
     *}
     */
    public static int random_test_valid(MemorySegment struct) {
        return struct.get(random_test_valid$LAYOUT, random_test_valid$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang = c:
     * unsigned int random_test_valid
     *}
     */
    public static void random_test_valid(MemorySegment struct, int fieldValue) {
        struct.set(random_test_valid$LAYOUT, random_test_valid$OFFSET, fieldValue);
    }

    private static final OfBoolean validated$LAYOUT = (OfBoolean) $LAYOUT.select(groupElement("validated"));

    /**
     * Layout for field:
     * {@snippet lang = c:
     * bool validated
     *}
     */
    public static final OfBoolean validated$layout() {
        return validated$LAYOUT;
    }

    private static final long validated$OFFSET = 32;

    /**
     * Offset for field:
     * {@snippet lang = c:
     * bool validated
     *}
     */
    public static final long validated$offset() {
        return validated$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang = c:
     * bool validated
     *}
     */
    public static boolean validated(MemorySegment struct) {
        return struct.get(validated$LAYOUT, validated$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang = c:
     * bool validated
     *}
     */
    public static void validated(MemorySegment struct, boolean fieldValue) {
        struct.set(validated$LAYOUT, validated$OFFSET, fieldValue);
    }

    private static final OfBoolean achievement$LAYOUT = (OfBoolean) $LAYOUT.select(groupElement("achievement"));

    /**
     * Layout for field:
     * {@snippet lang = c:
     * bool achievement
     *}
     */
    public static final OfBoolean achievement$layout() {
        return achievement$LAYOUT;
    }

    private static final long achievement$OFFSET = 33;

    /**
     * Offset for field:
     * {@snippet lang = c:
     * bool achievement
     *}
     */
    public static final long achievement$offset() {
        return achievement$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang = c:
     * bool achievement
     *}
     */
    public static boolean achievement(MemorySegment struct) {
        return struct.get(achievement$LAYOUT, achievement$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang = c:
     * bool achievement
     *}
     */
    public static void achievement(MemorySegment struct, boolean fieldValue) {
        struct.set(achievement$LAYOUT, achievement$OFFSET, fieldValue);
    }

    private static final OfBoolean cheat$LAYOUT = (OfBoolean) $LAYOUT.select(groupElement("cheat"));

    /**
     * Layout for field:
     * {@snippet lang = c:
     * bool cheat
     *}
     */
    public static final OfBoolean cheat$layout() {
        return cheat$LAYOUT;
    }

    private static final long cheat$OFFSET = 34;

    /**
     * Offset for field:
     * {@snippet lang = c:
     * bool cheat
     *}
     */
    public static final long cheat$offset() {
        return cheat$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang = c:
     * bool cheat
     *}
     */
    public static boolean cheat(MemorySegment struct) {
        return struct.get(cheat$LAYOUT, cheat$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang = c:
     * bool cheat
     *}
     */
    public static void cheat(MemorySegment struct, boolean fieldValue) {
        struct.set(cheat$LAYOUT, cheat$OFFSET, fieldValue);
    }

    private static final OfBoolean hardcoded$LAYOUT = (OfBoolean) $LAYOUT.select(groupElement("hardcoded"));

    /**
     * Layout for field:
     * {@snippet lang = c:
     * bool hardcoded
     *}
     */
    public static final OfBoolean hardcoded$layout() {
        return hardcoded$LAYOUT;
    }

    private static final long hardcoded$OFFSET = 35;

    /**
     * Offset for field:
     * {@snippet lang = c:
     * bool hardcoded
     *}
     */
    public static final long hardcoded$offset() {
        return hardcoded$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang = c:
     * bool hardcoded
     *}
     */
    public static boolean hardcoded(MemorySegment struct) {
        return struct.get(hardcoded$LAYOUT, hardcoded$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang = c:
     * bool hardcoded
     *}
     */
    public static void hardcoded(MemorySegment struct, boolean fieldValue) {
        struct.set(hardcoded$LAYOUT, hardcoded$OFFSET, fieldValue);
    }

    /**
     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
     */
    public static MemorySegment asSlice(MemorySegment array, long index) {
        return array.asSlice(layout().byteSize() * index);
    }

    /**
     * The size (in bytes) of this struct
     */
    public static long sizeof() {return layout().byteSize();}

    /**
     * Allocate a segment of size {@code layout().byteSize()} using {@code allocator}
     */
    public static MemorySegment allocate(SegmentAllocator allocator) {
        return allocator.allocate(layout());
    }

    /**
     * Allocate an array of size {@code elementCount} using {@code allocator}.
     * The returned segment has size {@code elementCount * layout().byteSize()}.
     */
    public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
        return reinterpret(addr, 1, arena, cleanup);
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code elementCount * layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup) {
        return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
    }
}
