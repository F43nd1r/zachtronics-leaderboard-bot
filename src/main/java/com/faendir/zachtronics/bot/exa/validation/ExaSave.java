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

package com.faendir.zachtronics.bot.exa.validation;

import com.faendir.zachtronics.bot.validation.ValidationException;
import com.google.common.io.LittleEndianDataOutputStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * <pre>
 * 4 bytes: solution format (exapunks has three different ones, 1006, 1007, and 1008, nowadays all solutions use 1008, but old solutions might still be in the old formats)
 * 4+N bytes: puzzle name (length then string)
 * 4+N bytes: solution name (length then string)
 * 4 bytes: number of hacker battle wins (hacker battles are not tracked on the leaderboard, so reject solutions that have a non-zero number here. not present in 1006!)
 * 4 bytes: sandbox mode indicator (same as above, reject non-zero. not present in 1006!)
 * 4 bytes: number of metrics (3 for solved files, so reject anything that’s not 3)
 * 4 bytes: always 0
 * 4 bytes: cycle count
 * 4 bytes: always 1
 * 4 bytes: line count
 * 4 bytes: always 2
 * 4 bytes: activity
 * 4 bytes: EXA count
 * N bytes: EXA data
 * </pre>
 */
@Data
@AllArgsConstructor
public class ExaSave {
    private final @NotNull String puzzle;
    private @NotNull String name;

    private int cycles;
    private int size;
    private int activity;

    private final @NotNull List<@NotNull ExaChip> chips;

    @Accessors(fluent = true)
    @Getter(lazy = true)
    private final int actualSize = chips.stream().mapToInt(ExaChip::size).sum();

    @NotNull
    public static ExaSave unmarshal(byte[] solution) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(solution).order(ByteOrder.LITTLE_ENDIAN);
        return unmarshal(byteBuffer);
    }

    static @NotNull ExaSave unmarshal(@NotNull ByteBuffer byteBuffer) {
        int version = byteBuffer.getInt();
        if (version < 1006 || version > 1008)
            throw new IllegalStateException("Invalid version: " + version);

        String puzzle = readString(byteBuffer);
        String name = readString(byteBuffer);

        if (version > 1006) {
            assertInt(byteBuffer, 0, v -> "Hacker battles won: " + v + " is not 0");
            assertInt(byteBuffer, 0, v -> "Sandbox indicator: " + v + " is not 0");
        }

        assertInt(byteBuffer, 3, v -> "Unsolved solution");

        assertInt(byteBuffer, 0, v -> "Cycles id: " + v + " is not 0");
        int cycles = byteBuffer.getInt();
        assertInt(byteBuffer, 1, v -> "Size id: " + v + " is not 1");
        int size = byteBuffer.getInt();
        assertInt(byteBuffer, 2, v -> "Activity id: " + v + " is not 2");
        int activity = byteBuffer.getInt();

        if (cycles == 0 || size == 0 || activity == 0)
            throw new ValidationException("Unsolved solution");

        int chipLength = byteBuffer.getInt();
        @Nullable ExaChip[] rawChips = new ExaChip[chipLength];
        for (int i = 0; i < chipLength; i++) {
            rawChips[i] = ExaChip.unmarshal(byteBuffer);
        }

        if (byteBuffer.hasRemaining())
            throw new IllegalStateException("Remaining data: " + byteBuffer.remaining() + " bytes");

        List<ExaChip> chips = Arrays.stream(rawChips).filter(Objects::nonNull).toList();
        return new ExaSave(puzzle, name, cycles, size, activity, chips);
    }

    public byte @NotNull [] marshal() {
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        try (LittleEndianDataOutputStream out = new LittleEndianDataOutputStream(ba)) {
            marshal(out);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return ba.toByteArray();
    }

    private void marshal(@NotNull DataOutput out) throws IOException {
        out.writeInt(1008);
        writeString(out, puzzle);
        writeString(out, name);
        out.writeInt(0); // hacker battles
        out.writeInt(0); // sandbox mode
        out.writeInt(3); // metrics
        out.writeInt(0); // id
        out.writeInt(cycles);
        out.writeInt(1); // id
        out.writeInt(size);
        out.writeInt(2); // id
        out.writeInt(activity);
        out.writeInt(chips.size());
        for (ExaChip chip : chips) {
            chip.marshal(out);
        }
    }

    static @NotNull String readString(@NotNull ByteBuffer byteBuffer) {
        int length = byteBuffer.getInt();
        String result = new String(byteBuffer.array(), byteBuffer.arrayOffset() + byteBuffer.position(), length);
        byteBuffer.position(byteBuffer.position() + length);
        return result;
    }

    static void writeString(@NotNull DataOutput out, @NotNull String string) throws IOException {
        byte[] data = string.getBytes();
        out.writeInt(data.length);
        out.write(data);
    }

    static void assertInt(@NotNull ByteBuffer byteBuffer, int value, IntFunction<String> rejectReason) {
        int read = byteBuffer.getInt();
        if (read != value)
            throw new ValidationException(rejectReason.apply(read));
    }
}
