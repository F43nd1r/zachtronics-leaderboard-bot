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

import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * <pre>
 *  1 byte: normally 0xA, 0xB appears on version 1007 and marks an empty EXA
 *  4+N bytes: EXA name (length then string)
 *  4+N bytes: EXA code (length then string)
 *  1 byte: irrelevant (editor display status: 0 unrolled, 1 collapsed)
 *  1 byte: communication mode (0 is GLOBAL, 1 is LOCAL)
 *  100 bytes: irrelevant (image data for sandbox mode)
 * </pre>
 */
@Value
public class ExaChip {
    @NotNull String name;
    @NotNull String code;
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

        return new ExaChip(name, code, globalCommMode);
    }

    void marshal(@NotNull DataOutput out) throws IOException {
        out.writeByte(0xA);
        ExaSave.writeString(out, name);
        ExaSave.writeString(out, code);
        out.writeByte(0);
        out.writeByte(globalCommMode ? 0 : 1);
        out.write(new byte[100]);
    }

    public int size() {
        int result = 0;
        int repMulti = 1;

        for (String rawLine : code.split("\\r?\\n")) {
            String line = StringUtils.substringBefore(rawLine, ';').trim(); // strip away partial comments
            if (line.isEmpty() || line.startsWith("NOTE")) {
                // effectively empty lines
            }
            else if (line.startsWith("@REP")) {
                repMulti = Integer.parseInt(line.substring(5));
            }
            else if (line.equals("@END")) {
                repMulti = 1;
            }
            else {
                result += repMulti;
            }
        }
        return result;
    }

}
