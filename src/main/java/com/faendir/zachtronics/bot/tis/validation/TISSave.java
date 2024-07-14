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

package com.faendir.zachtronics.bot.tis.validation;

import com.faendir.zachtronics.bot.validation.ValidationException;
import lombok.Value;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
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

    private static final Pattern LINE_REGEX = Pattern.compile(
        String.join("\\s*", new String[]{"", "(?:(?<label>[^#:]+):)?", "(?<code>[^#:]+)?", "(?:#(?<comment>.*))?", ""}));

    @NotNull List<List<TISCodeLine>> codeByNode;
    @IntRange(from = 0, to = MAX_NODES) int nodes;
    @IntRange(from = 0, to = TISSave.MAX_NODES * TISSave.MAX_NODE_INSTR) int instructions;


    @Value
    static class TISCodeLine {
        @NotNull String rawLine;

        @Nullable String label;
        @Nullable String code;
        @Nullable String comment;
    }

    @NotNull
    public static TISSave unmarshal(@NotNull String solution) {
        List<List<TISCodeLine>> codeByNode = IntStream.range(0, MAX_NODES)
                                                      .<List<TISCodeLine>>mapToObj(i -> new ArrayList<>())
                                                      .toList();
        int currentNode = -1;

        for (String line: solution.split("\r?\n")) {
            if (line.startsWith("@")) {
                currentNode = Integer.parseInt(line.substring(1));
                if (currentNode < 0 || currentNode >= MAX_NODES)
                    throw new ValidationException("Invalid node id: @" + currentNode);
            }
            else {
                Matcher m = LINE_REGEX.matcher(line);
                if (m.matches()) {
                    codeByNode.get(currentNode).add(new TISCodeLine(line, m.group("label"), m.group("code"), m.group("comment")));
                }
                else
                    throw new ValidationException("Malformed line: \"" + line + "\"");
            }
        }

        int nodes = 0;
        int instructions = 0;
        for (int i = 0; i < codeByNode.size(); i++) {
            long nodeInstructions = codeByNode.get(i).stream()
                                              .filter(l -> l.code != null)
                                              .count();
            if (nodeInstructions > 0) {
                if (nodeInstructions > MAX_NODE_INSTR)
                    throw new ValidationException("Too many lines for node @" + i + ": " + nodeInstructions);
                nodes++;
                instructions += nodeInstructions;
            }
        }

        return new TISSave(codeByNode, nodes, instructions);
    }

    @NotNull Stream<String> codeAsStream() {
        return codeByNode.stream()
                         .flatMap(List::stream)
                         .map(TISCodeLine::getCode)
                         .filter(Objects::nonNull);
    }
}
