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
 * Decal
 * {
 *     uint8 Facing
 *     int16 Type
 * }
 * </pre>
 */
@Value
class IfDecal {
    byte facing;
    short type;

    public static IfDecal unmarshal(@NotNull ByteBuffer byteBuffer) {
        return new IfDecal(byteBuffer.get(), byteBuffer.getShort());
    }
}
