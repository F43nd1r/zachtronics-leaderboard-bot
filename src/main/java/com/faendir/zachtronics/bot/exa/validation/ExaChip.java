/*
 * Copyright (c) 2024
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

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * <pre>
 *  1 byte: marks an empty EXA if it's 11, appears on version 1007
 *  4+N bytes: EXA name (length then string)
 *  4+N bytes: EXA code (length then string)
 *  1 byte: irrelevant
 *  1 byte: communication mode (0 is GLOBAL, 1 is LOCAL)
 *  100 bytes: irrelevant (image data for sandbox mode)
 * </pre>
 */
@Value
public class ExaChip {
    @NotNull String name;
    @NotNull String[] lines;
    boolean globalCommMode;

    @Nullable
    static ExaChip unmarshal(@NotNull ByteBuffer byteBuffer) {
        if (byteBuffer.get() == 0xB) {
            // empty EXA of some sort, burn 4B and go on
            byteBuffer.position(byteBuffer.position() + 4);
            return null;
        }
        String name = ExaSave.readString(byteBuffer);
        String code = ExaSave.readString(byteBuffer);
        byteBuffer.position(byteBuffer.position() + 1);
        boolean globalCommMode = byteBuffer.get() == 0;
        byteBuffer.position(byteBuffer.position() + 100);

        String[] lines = code.split("\\r?\\n");
        return new ExaChip(name, lines, globalCommMode);
    }

    int size() {
        int result = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("NOTE") || line.startsWith(";")) {
                continue;
            }
            if (line.startsWith("@REP")) {
                int repTimes = Integer.parseInt(line.substring(5));
                for (i++; i < lines.length; i++) {
                    if (lines[i].trim().equals("@END"))
                        break;
                    result += repTimes;
                }
                continue;
            }
            result++;
        }
        return result;
    }

}
