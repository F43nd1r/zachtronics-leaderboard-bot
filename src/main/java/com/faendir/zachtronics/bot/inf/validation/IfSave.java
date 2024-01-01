/*
 * Copyright (c) 2022
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

package com.faendir.zachtronics.bot.inf.validation;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The solution has the following representation (all integers are little endian)
 *
 * <pre>
 * Save
 * {
 *     int32 Version
 *     int32 BlockCount
 *     Block[] Blocks
 * }
 *
 * Block
 * {
 *     int16 Type
 *     int16 PositionX
 *     int16 PositionY
 *     int16 PositionZ
 *     uint8 Facing
 *     uint8 ToggleState
 *     uint8 DecalCount
 *     Decal[] Decals
 * }
 *
 * Decal
 * {
 *     uint8 Facing
 *     int16 Type
 * }
 * </pre>
 */
@Value
public class IfSave {
    int version;
    @NotNull IfBlock[] blocks;

    @NotNull
    public static IfSave unmarshal(String solution) {
        byte[] bytes = Base64.getDecoder().decode(solution);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return unmarshal(byteBuffer);
    }

    @NotNull
    static IfSave unmarshal(@NotNull ByteBuffer byteBuffer) {
        int version = byteBuffer.getInt();
        if (version != 3)
            throw new IllegalStateException("Version is not 3");

        int blockCount = byteBuffer.getInt();

        IfBlock[] blocks = new IfBlock[blockCount];
        for (int i = 0; i < blockCount; i++) {
            blocks[i] = IfBlock.unmarshal(byteBuffer);
        }

        if (byteBuffer.hasRemaining())
            throw new IllegalStateException("Remaining data: " + byteBuffer.remaining() + " bytes");

        return new IfSave(version, blocks);
    }

    public int blockScore() {
        return (int) Arrays.stream(blocks).filter(b -> b.getType() != IfBlockType.PLATFORM).count();
    }

    public int footprintLowerBound() {
        return (int) Arrays.stream(blocks)
                           .map(b -> (b.getPositionX() << 16) | (b.getPositionZ() & 0xFFFF))
                           .distinct().count();
    }

    /**
     * Checks if the save has both a rotator and a welder, which are prerequisites to realize GRA:<br>
     * A GRA consists in rotating an object made up of both factory and input blocks
     */
    public boolean couldHaveGRA() {
        Set<Short> types = Arrays.stream(blocks)
                                 .map(IfBlock::getType)
                                 .collect(Collectors.toSet());
        return (types.contains(IfBlockType.ROTATOR_CW) || types.contains(IfBlockType.ROTATOR_CCW)) &&
               (types.contains(IfBlockType.WELDER_A) || types.contains(IfBlockType.WELDER_B));
    }
}
