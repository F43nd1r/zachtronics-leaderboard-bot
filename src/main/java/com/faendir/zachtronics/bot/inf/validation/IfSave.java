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

    static IfSave unmarshal(@NotNull ByteBuffer byteBuffer) {
        int version = byteBuffer.getInt();
        if (version != 3)
            throw new IllegalStateException("Version is not 3");

        int blockCount = byteBuffer.getInt();

        IfBlock[] blocks = new IfBlock[blockCount];
        for (int i = 0; i < blockCount; i++) {
            blocks[i] = IfBlock.unmarshal(byteBuffer);
        }

        return new IfSave(version, blocks);
    }

    public int blockScore() {
        return (int) Arrays.stream(blocks).filter(b -> b.getType() != IfBlockType.PLATFORM).count();
    }
}
