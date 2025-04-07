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

package com.faendir.zachtronics.bot.tis.ai;

import com.faendir.zachtronics.bot.validation.ValidationException;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * The solution has the following format, @ is guaranteed not to be in the actual code:
 *
 * <pre>{@code
 * @0
 *
 *
 * @1
 * MOV UP    ACC
 * SUB RIGHT
 * MOV ACC   DOWN
 *
 * @2
 * MOV UP LEFT
 *
 * @3
 *
 *
 * @4
 *
 *
 * @5
 * MOV UP  DOWN
 *
 * #MOV UP  ACC
 * #MOV ACC DOWN
 * #MOV ACC DOWN
 *
 * @6
 *
 *
 * @7
 *
 *
 * }</pre>
 * up to <tt>@11</tt>, with a max of 15 instr/node
 */
@Value
public class TISSave {
    public static final int MAX_NODES = 12;
    public static final int MAX_NODE_INSTR = 15;

    @NotNull List<TISNode> nodes;
    int instructions;

    @Value
    static class TISNode {
        int id;
        @NotNull List<TISLine> lines = new ArrayList<>();

        Stream<TISLine> codeAsStream() {
            return lines.stream().filter(TISLine::isCode);
        }
    }

    public static @NotNull TISSave unmarshal(@NotNull Path solution) throws IOException {
        return unmarshal(Files.readAllLines(solution));
    }

    public static @NotNull TISSave unmarshal(@NotNull String solution) {
        return unmarshal(Arrays.asList(solution.split("\r?\n")));
    }

    public static @NotNull TISSave unmarshal(@NotNull List<String> lines) {
        List<TISNode> nodes = new ArrayList<>(Collections.nCopies(MAX_NODES, null));

        TISNode currentNode = null;
        int instructions = 0;

        for (String line : lines) {
            if (line.startsWith("@")) {
                int id = Integer.parseInt(line.substring(1));
                if (id < 0 || id >= MAX_NODES)
                    throw new ValidationException("Invalid node id: @" + id);
                currentNode = new TISNode(id);
                nodes.set(id, currentNode);
            }
            else {
                assert currentNode != null;
                TISLine cl = TISLine.unmarshal(line);
                currentNode.getLines().add(cl);
                if (cl.isCode()) {
                    instructions++;
                }
            }
        }
        nodes.removeIf(Objects::isNull);
        return new TISSave(nodes, instructions);
    }

}
