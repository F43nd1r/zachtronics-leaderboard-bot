/*
 * Copyright (c) 2026
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
class TISSave {
    static final int MAX_NODES = 12;
    static final int MAX_NODE_INSTR = 15;

    @NotNull List<TISNode> nodes;
    int instructions;

    Stream<TISLine> codeAsStream() {
        return nodes.stream().flatMap(node -> node.getCodeLines().stream());
    }

    static @NotNull TISSave unmarshal(@NotNull String solution) {
        List<TISNode> nodes = new ArrayList<>(Collections.nCopies(MAX_NODES, null));

        TISNode currentNode = null;
        int instructions = 0;

        for (String line : solution.split("\r?\n")) {
            if (line.startsWith("@")) {
                int id = Integer.parseInt(line.substring(1));
                if (id < 0 || id >= MAX_NODES)
                    throw new ValidationException("Invalid node id: @" + id);
                currentNode = new TISNode(id);
                nodes.set(id, currentNode);
            }
            else if (currentNode != null) {
                TISLine cl = TISLine.unmarshal(line);
                currentNode.getLines().add(cl);
                if (cl.isCode()) {
                    currentNode.getCodeLines().add(cl);
                    instructions++;
                }
            }
        }
        nodes.removeIf(Objects::isNull);
        for (TISNode node : nodes) {
            // clean up ending newlines but leave nodes not fully empty
            for (int l = node.getLines().size() - 1; l >= 1; l--) {
                if (node.getLines().get(l).getRawLine().isEmpty())
                    node.getLines().remove(l);
                else
                    break;
            }
        }
        return new TISSave(nodes, instructions);
    }

    @NotNull String marshal() {
        StringBuilder sb = new StringBuilder();
        for (TISNode node : nodes) {
            node.marshalInto(sb);
        }
        return sb.toString();
    }
}
