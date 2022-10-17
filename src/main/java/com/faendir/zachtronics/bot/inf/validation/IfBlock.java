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

/**
 * <pre>
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
 * </pre>
 */
@Value
class IfBlock {
    short type;
    short positionX;
    short positionY;
    short positionZ;
    byte facing;
    byte toggleState;
    @NotNull IfDecal[] decals;

    public static IfBlock unmarshal(@NotNull ByteBuffer byteBuffer) {
        short type = byteBuffer.getShort();
        short positionX = byteBuffer.getShort();
        short positionY = byteBuffer.getShort();
        short positionZ = byteBuffer.getShort();
        byte facing = byteBuffer.get();
        byte toggleState = byteBuffer.get();
        byte decalCount = byteBuffer.get();

        IfDecal[] decals = new IfDecal[decalCount];
        for (int i = 0; i < decalCount; i++) {
            decals[i] = IfDecal.unmarshal(byteBuffer);
        }

        return new IfBlock(type, positionX, positionY, positionZ, facing, toggleState, decals);
    }
}
