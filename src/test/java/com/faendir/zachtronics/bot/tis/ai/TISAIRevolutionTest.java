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

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.tis.ai.TISSave.TISNode;
import com.faendir.zachtronics.bot.tis.model.TISPuzzle;
import com.faendir.zachtronics.bot.tis.model.TISSubmission;
import com.faendir.zachtronics.bot.tis.repository.TISSolutionRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.SplittableRandom;

import static com.faendir.zachtronics.bot.tis.ai.TISLine.TISOperation.*;
import static com.faendir.zachtronics.bot.tis.ai.TISLine.TISPort.ACC;
import static com.faendir.zachtronics.bot.tis.ai.TISLine.TISPort.IMMEDIATE;

@BotTest
@Disabled("The AI Revolution is (ironically) triggered only by humans")
class TISAIRevolutionTest {

    @Autowired
    private TISSolutionRepository repository;

    @Test
    public void theAIRevolution() throws IOException {
        /*
        rm -rf /tmp/macina; mkdir -p /tmp/macina
        git ls-files --others --exclude-standard | xargs -I% cp % /tmp/macina
        git diff @~20 --no-renames --diff-filter=A --name-only | xargs -I% cp % /tmp/macina
        rsync -a --delete ../tis100/leaderboard/* src/test/resources/repositories/tis-leaderboard/
         */
        String author = "12345AIII";
        boolean noop = false;
        int deleteMin = 1;
        int deleteMax = 0;
        int swapBlockMin = 1;
        int swapBlockMax = 0;
        boolean moveLabelOnSwap = true;
        boolean fuseMovMovAcc = false; // fuse MOV X ACC; MOV ACC Y into MOV X Y
        boolean movAddSwap = false; // swap MOV X ACC; ADD Y with MOV Y ACC; ADD X
        int fuzzImmediates = 0; // move immediates around by up to this value one by one
        int fuzzTries = 0;
        int fuzzDelta = 1;
        boolean swapIf = false; // swap branches of JEZ/JNZ->JMP if-like structures
        Path puzzlePath = Paths.get("/tmp/macina");

        for (TISPuzzle puzzle : repository.getTrackedPuzzles()) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(puzzlePath, puzzle.getId() + "*.txt")) {
                for (Path path : paths) {
                    TISSave save = TISSave.unmarshal(path);
                    if (noop) {
                        String descr = path.getFileName().toString()
                                           .replace(puzzle.getId() + ".", "");
                        submit(Files.readString(path), puzzle, author, descr);
                    }
                    for (int delAmount = deleteMin; delAmount <= deleteMax; delAmount++) {
                        for (int delStart = 0; delStart + delAmount - 1 < save.getInstructions(); delStart++) {
                            StringBuilder data = genTrialDeletion(save, delStart, delAmount);
                            String descr = path.getFileName().toString()
                                               .replace(puzzle.getId() + ".", "")
                                               .replace(".txt", "?d" + delStart + "-" + (delStart + delAmount - 1));
                            submit(data, puzzle, author, descr);
                        }
                    }
                    for (int swapBlockSize = swapBlockMin; swapBlockSize <= swapBlockMax; swapBlockSize++) {
                        for (int codeLineIdx = 0; codeLineIdx + swapBlockSize < save.getInstructions(); codeLineIdx++) {
                            StringBuilder data = genSwapToEnd(save, codeLineIdx, swapBlockSize, moveLabelOnSwap);
                            String descr = path.getFileName().toString()
                                               .replace(puzzle.getId() + ".", "")
                                               .replace(".txt", "?s" + codeLineIdx + "+" + swapBlockSize);
                            submit(data, puzzle, author, descr);
                        }
                    }
                    if (fuseMovMovAcc) {
                        int foundLine = 0;
                        boolean found = true;
                        while (found) {
                            found = false;
                            int countLine = 0;
                            StringBuilder data = new StringBuilder();
                            for (TISNode node : save.getNodes()) {
                                data.append('@').append(node.getId()).append('\n');
                                @NotNull List<TISLine> lines = node.getLines();
                                for (int i = 0; i < lines.size(); i++) {
                                    TISLine line = lines.get(i);
                                    countLine++;
                                    if (found || countLine < foundLine) {
                                        //
                                    }
                                    else if (line.is(MOV, null, ACC) && i + 1 < lines.size()) {
                                        TISLine nextLine = lines.get(i + 1);
                                        if (nextLine.is(MOV, ACC, null)) {
                                            // collapse
                                            if (line.getLabel() != null)
                                                data.append(line.getLabel()).append(": ");
                                            if (nextLine.getLabel() != null)
                                                data.append(nextLine.getLabel()).append(": ");
                                            data.append("MOV ").append(line.arg1()).append(" ")
                                                .append(nextLine.arg2()).append('\n');
                                            found = true;
                                            foundLine = countLine + 1;
                                            i++;
                                            continue;
                                        }
                                    }
                                    data.append(line.getRawLine()).append('\n');
                                }
                            }
                            if (found) {
                                String descr = path.getFileName().toString()
                                                   .replace(puzzle.getId() + ".", "")
                                                   .replace(".txt", "?FMM" + foundLine);
                                submit(data, puzzle, author, descr);
                            }
                        }
                    }
                    if (movAddSwap) {
                        int foundLine = 0;
                        boolean found = true;
                        while (found) {
                            found = false;
                            int countLine = 0;
                            TISLine movLine = null;
                            StringBuilder data = new StringBuilder();
                            for (TISNode node : save.getNodes()) {
                                data.append('@').append(node.getId()).append('\n');
                                for (TISLine line : node.getLines()) {
                                    if (line.isCode()) {
                                        countLine++;
                                        if (found || countLine < foundLine) {
                                            //
                                        }
                                        else if (movLine != null) {
                                            if ((line.is(ADD) || line.is(SUB)) && line.getPort1() != ACC) {
                                                if (movLine.getLabel() != null)
                                                    data.append(movLine.getLabel()).append(": ");
                                                String movArg =
                                                    line.is(SUB, IMMEDIATE) ? String.valueOf(-line.getImmediate()) : line.arg1();
                                                data.append("MOV ").append(movArg).append(" ACC").append('\n');
                                                if (line.is(SUB) && line.getPort1() != IMMEDIATE)
                                                    data.append("NEG\n");
                                                if (line.getLabel() != null)
                                                    data.append(line.getLabel()).append(": ");
                                                data.append("ADD ").append(movLine.arg1()).append('\n');
                                                foundLine = countLine;
                                                found = true;
                                                continue;
                                            }
                                            else {
                                                data.append(movLine.getRawLine()).append('\n');
                                                movLine = null;
                                            }
                                        }
                                        else if (line.is(MOV, null, ACC)) {
                                            movLine = line;
                                            continue;
                                        }
                                    }
                                    data.append(line.getRawLine()).append('\n');
                                }
                            }
                            if (found) {
                                String descr = path.getFileName().toString()
                                                   .replace(puzzle.getId() + ".", "")
                                                   .replace(".txt", "?MA" + foundLine);
                                submit(data, puzzle, author, descr);
                            }
                        }
                    }
                    for (int fuzzFactor = -fuzzImmediates; fuzzFactor <= fuzzImmediates; fuzzFactor++) {
                        if (fuzzFactor == 0)
                            continue;
                        int foundLine = 0;
                        boolean found = true;
                        while (found) {
                            found = false;
                            int countLine = 0;
                            StringBuilder data = new StringBuilder();
                            for (TISNode node : save.getNodes()) {
                                data.append('@').append(node.getId()).append('\n');
                                for (TISLine line : node.getLines()) {
                                    if (hasFuzzableArg(line)) {
                                        countLine++;
                                        if (found || countLine < foundLine) {
                                            //
                                        }
                                        else {
                                            String fuzzedLine = line.getRawLine()
                                                                    .replace(String.valueOf(line.getImmediate()),
                                                                             String.valueOf(line.getImmediate() + fuzzFactor));
                                            data.append(fuzzedLine).append('\n');
                                            foundLine = countLine + 1;
                                            found = true;
                                            continue;
                                        }
                                    }
                                    data.append(line.getRawLine()).append('\n');
                                }
                            }
                            if (found) {
                                String descr = path.getFileName().toString()
                                                   .replace(puzzle.getId() + ".", "")
                                                   .replace(".txt", "?F" + fuzzFactor + "L" + foundLine);
                                submit(data, puzzle, author, descr);
                            }
                        }
                    }
                    if (fuzzTries != 0) {
                        SplittableRandom rng = new SplittableRandom();
                        long nums = save.getNodes().stream()
                                        .flatMap(TISNode::codeAsStream)
                                        .filter(TISAIRevolutionTest::hasFuzzableArg).count();
                        for (int i = 0; i < fuzzTries * nums; i++) {
                            StringBuilder data = new StringBuilder();
                            for (TISNode node : save.getNodes()) {
                                data.append('@').append(node.getId()).append('\n');
                                for (TISLine line : node.getLines()) {
                                    if (hasFuzzableArg(line)) {
                                        int a = Math.abs(line.getImmediate());
                                        int val = rng.nextInt(a * 9 / 10 - fuzzDelta, a * 11 / 10 + fuzzDelta);
                                        if (line.getImmediate() < 0) val *= -1;
                                        String fuzzedLine = line.getRawLine()
                                                                .replace(String.valueOf(line.getImmediate()),
                                                                         String.valueOf(val));
                                        data.append(fuzzedLine).append('\n');
                                    }
                                    else
                                        data.append(line.getRawLine()).append('\n');
                                }
                            }
                            String descr = path.getFileName().toString()
                                               .replace(puzzle.getId() + ".", "")
                                               .replace(".txt", "?FD" + fuzzDelta + "T" + i);
                            submit(data, puzzle, author, descr);
                        }
                    }
                    if (swapIf) {
                        int foundLine = 0;
                        boolean found = true;
                        while (found) {
                            found = false;
                            int countLine = 0;
                            int jmp1Idx = -1;
                            StringBuilder data = new StringBuilder();
                            for (TISNode node : save.getNodes()) {
                                data.append('@').append(node.getId()).append('\n');
                                jmp1Idx = -1;
                                @NotNull List<TISLine> lines = node.getLines();
                                for (int i = 0; i < lines.size(); i++) {
                                    TISLine line = lines.get(i);
                                    if (line.isCode()) {
                                        countLine++;
                                        if (found || countLine < foundLine) {
                                            //
                                        }
                                        else if (line.is(JEZ) || line.is(JNZ)) {
                                            // start of IF
                                            jmp1Idx = i;
                                            continue;
                                        }
                                        else if (jmp1Idx != -1) {
                                            if (line.is(JMP)) {
                                                // jump point of branch 1
                                                int jmp2Idx = i;
                                                int onePastTheEndIdx = 0;
                                                String endLabel = line.getJumpTarget();
                                                for (; onePastTheEndIdx < lines.size(); onePastTheEndIdx++) {
                                                    if (endLabel.equals(lines.get(onePastTheEndIdx).getLabel())) {
                                                        // has to exist, or the solution would be invalid
                                                        break;
                                                    }
                                                }
                                                if (onePastTheEndIdx < jmp2Idx) {
                                                    // jump goes backwards, give up
                                                    for (int j = jmp1Idx; j <= i; j++) {
                                                        data.append(lines.get(j).getRawLine()).append('\n');
                                                    }
                                                    jmp1Idx = -1;
                                                    continue;
                                                }

                                                assert lines.get(jmp1Idx).getOp() != null;
                                                String otherJump = switch (lines.get(jmp1Idx).getOp()) {
                                                    case JEZ -> "JNZ";
                                                    case JNZ -> "JEZ";
                                                    case JGZ -> "JLZ"; // inexact
                                                    case JLZ -> "JGZ"; // inexact
                                                    default ->
                                                        throw new IllegalStateException("Unexpected value: " + lines.get(jmp1Idx).getOp());
                                                };
                                                data.append(otherJump).append(" ").append(lines.get(jmp1Idx).getLabel()).append('\n');
                                                for (int j = jmp2Idx + 1; j < onePastTheEndIdx; j++) {
                                                    data.append(lines.get(j).getRawLine()).append('\n');
                                                }
                                                data.append("JMP ").append(endLabel).append('\n');
                                                data.append(lines.get(jmp1Idx).getLabel()).append(": ");
                                                for (int j = jmp1Idx + 1; j < jmp2Idx; j++) {
                                                    data.append(lines.get(j).getRawLine()).append('\n');
                                                }
                                                found = true;
                                                foundLine = countLine + onePastTheEndIdx - jmp1Idx;
                                                i = onePastTheEndIdx;
                                            }
                                            else
                                                continue;
                                        }
                                    }
                                    data.append(line.getRawLine()).append('\n');
                                }
                            }
                            if (found) {
                                String descr = path.getFileName().toString()
                                                   .replace(puzzle.getId() + ".", "")
                                                   .replace(".txt", "?SIF" + jmp1Idx + "-" + foundLine);
                                submit(data, puzzle, author, descr);
                            }
                        }
                    }
                }
            }
        }

        /*
        rm -r src/test/resources/repositories/tis-leaderboard/*
        git restore --source=HEAD --staged --worktree -- src/test/resources/repositories/tis-leaderboard/
        rsync -a --delete --exclude=README.txt $(ls -1dt /tmp/tis-leaderboard* | head -n1)/* ../tis100/leaderboard/
         */

        System.out.println("Done " + count);
    }

    private static boolean hasFuzzableArg(@NotNull TISLine line) {
        return line.is(ADD, IMMEDIATE) || line.is(SUB, IMMEDIATE);
    }

    private static @NotNull StringBuilder genSwapToEnd(@NotNull TISSave save, int codeLineIdx, int swapBlockSize, boolean moveLabelOnSwap) {
        int codeCount = 0;
        TISLine swappedLine = null;
        StringBuilder data = new StringBuilder();
        for (TISNode node : save.getNodes()) {
            data.append('@').append(node.getId()).append('\n');
            for (TISLine line : node.getLines()) {
                if (line.isCode()) {
                    codeCount++;
                    if (codeCount - 1 == codeLineIdx) {
                        swappedLine = line;
                        if (!moveLabelOnSwap && line.getLabel() != null)
                            data.append(line.getLabel()).append(": ");
                        continue;
                    }
                    data.append(line.getRawLine()).append('\n');
                    if (codeCount - 1 == codeLineIdx + swapBlockSize) {
                        assert swappedLine != null;
                        if (moveLabelOnSwap)
                            data.append(swappedLine.getRawLine());
                        else
                            data.append(swappedLine.code());
                        data.append('\n');
                    }
                }
                else {
                    data.append(line.getRawLine()).append('\n');
                }
            }
        }
        return data;
    }

    private static @NotNull StringBuilder genTrialDeletion(@NotNull TISSave save, int delStart, int delAmount) {
        int codeCount = 0;
        StringBuilder data = new StringBuilder();
        for (TISNode node : save.getNodes()) {
            data.append('@').append(node.getId()).append('\n');
            for (TISLine line : node.getLines()) {
                if (line.isCode()) {
                    codeCount++;
                    if (codeCount >= delStart + 1 && codeCount <= delStart + delAmount) {
                        if (line.getLabel() != null)
                            data.append(line.getLabel()).append(":\n");
                        continue;
                    }
                }
                data.append(line.getRawLine()).append('\n');
            }
        }
        return data;
    }

    private static int count = 0;

    private void submit(CharSequence data, @NotNull TISPuzzle puzzle, String author, String descr) throws IOException {
        count++;
//        Files.writeString(Path.of("/tmp/res", puzzle.getId() + "-" + descr + ".txt"), data);
        TISSubmission submission;
        try {
            submission = TISSubmission.fromData(data.toString(), puzzle, author + "?" + descr, null);
        }
        catch (Exception e) {
            return;
        }
        System.out.println(repository.submit(submission));
    }
}