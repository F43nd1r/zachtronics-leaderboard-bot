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
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.faendir.zachtronics.bot.tis.ai.TISLine.TISOperation.MOV;
import static com.faendir.zachtronics.bot.tis.ai.TISLine.TISPort.IMMEDIATE;
import static com.faendir.zachtronics.bot.tis.ai.TISLine.TISPort.LABEL;

@Value
class TISLine {
    private static final Pattern LINE_REGEX = Pattern.compile(
        "[\\s!]*(?:(?<label>[^#:]+):)?" +
        "[\\s!]*(?<code>(?<op>[A-Z]{3})" +
        "(?:[\\s,!]+(?<arg1>[^#:\\s,!]+))?" +
        "(?:[\\s,!]+(?<arg2>[^#:\\s,!]+))?)?" +
        "[\\s,!]*(?:#(?<comment>.*))?");

    @NotNull String rawLine;

    @Nullable String label;
    @Nullable String comment;

    @Nullable TISOperation op;
    @Nullable TISPort port1;
    short immediate;
    @Nullable String jumpTarget;
    @Nullable TISPort port2;

    /** we assume m matches */
    static @NotNull TISLine unmarshal(@NotNull String line) {
        Matcher m = LINE_REGEX.matcher(line);
        if (m.matches()) {
            String label = m.group("label");
            String comment = m.group("comment");

            TISOperation op = null;
            TISPort port1 = null;
            short immediate = Short.MIN_VALUE;
            String jumpTarget = null;
            TISPort port2 = null;
            if (m.group("code") != null) {
                op = TISOperation.valueOf(m.group("op"));
                String arg1 = m.group("arg1");
                String arg2 = m.group("arg2");
                if (op.arity == 0 && (arg1 != null || arg2 != null) ||
                    op.arity == 1 && (arg1 == null || arg2 != null) ||
                    op.arity == 2 && (arg1 == null || arg2 == null)) {
                    throw new ValidationException("Invalid arguments: \"" + m.group("code") + "\"");
                }
                switch (op) {
                    case ADD, SUB, MOV, JRO -> {
                        assert arg1 != null;
                        if (arg1.matches("[+-]?\\d+")) {
                            port1 = IMMEDIATE;
                            immediate = (short) Math.clamp(Integer.parseInt(arg1), -999, +999);
                        }
                        else {
                            port1 = parsePort(arg1);
                        }
                        if (op == MOV) {
                            assert arg2 != null;
                            port2 = parsePort(arg2);
                        }
                    }
                    case JMP, JEZ, JNZ, JGZ, JLZ -> {
                        port1 = LABEL;
                        jumpTarget = arg1;
                    }
                }
            }
            return new TISLine(line, label, comment, op, port1, immediate, jumpTarget, port2);
        }
        else
            throw new ValidationException("Malformed line: \"" + line + "\"");
    }

    private static TISPort parsePort(String port) {
        try {
            return TISPort.valueOf(port);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid port: " + port);
        }
    }

    String arg1() {
        assert port1 != null;
        return switch (port1) {
            case LABEL -> jumpTarget;
            case IMMEDIATE -> Short.toString(immediate);
            default -> port1.name();
        };
    }

    String arg2() {
        assert port2 != null;
        return port2.name();
    }

    @NotNull String code() {
        assert op != null;
        return op + (port1 == null ? "" : " " + arg1()) + (port2 == null ? "" : " " + arg2());
    }

    String marshal() {
        StringJoiner sj = new StringJoiner(" ");
        if (label != null) {
            sj.add(label + ":");
        }
        if (op != null) {
            sj.add(code());
        }
        if (comment != null) {
            sj.add("#" + comment);
        }
        return sj.toString();
    }

    boolean isCode() {
        return op != null;
    }

    boolean is(@NotNull TISOperation op) {
        return op == this.op;
    }

    boolean is(TISOperation op, @NotNull TISPort port1) {
        return (op == null || op == this.op) &&
               port1 == this.port1;
    }

    boolean is(TISOperation op, TISPort port1, TISPort port2) {
        return (op == null || op == this.op) &&
               (port1 == null || port1 == this.port1) &&
               (port2 == null || port2 == this.port2);
    }

    boolean isAny(TISOperation... ops) {
        return Arrays.asList(ops).contains(op);
    }

    @RequiredArgsConstructor
    enum TISOperation {
        HCF(0),
        NOP(0),
        SWP(0),
        SAV(0),
        NEG(0),

        MOV(2),

        ADD(1),
        SUB(1),

        JMP(1),
        JEZ(1),
        JNZ(1),
        JGZ(1),
        JLZ(1),

        JRO(1);

        final int arity;
    }

    enum TISPort {
        LEFT,
        RIGHT,
        UP,
        DOWN,

        NIL,
        ACC,
        ANY,
        LAST,

        LABEL,
        IMMEDIATE
    }
}
