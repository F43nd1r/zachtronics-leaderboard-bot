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
import com.faendir.zachtronics.bot.TestConfigurationKt;
import com.faendir.zachtronics.bot.config.GitProperties;
import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.repository.SubmitResult;
import com.faendir.zachtronics.bot.tis.ai.TISLine.TISOperation;
import com.faendir.zachtronics.bot.tis.ai.TISLine.TISPort;
import com.faendir.zachtronics.bot.tis.ai.TISSave.TISNode;
import com.faendir.zachtronics.bot.tis.model.TISCategory;
import com.faendir.zachtronics.bot.tis.model.TISPuzzle;
import com.faendir.zachtronics.bot.tis.model.TISRecord;
import com.faendir.zachtronics.bot.tis.model.TISSubmission;
import com.faendir.zachtronics.bot.tis.repository.TISSolutionRepository;
import com.faendir.zachtronics.bot.validation.ValidationException;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.faendir.zachtronics.bot.tis.ai.TISLine.TISOperation.*;
import static com.faendir.zachtronics.bot.tis.ai.TISLine.TISPort.*;

@BotTest
@Disabled("The AI Revolution is (ironically) triggered only by humans")
class TISAIRevolutionTest {

    @Autowired
    private TISSolutionRepository repository;

    @TestConfiguration
    static class RepositoryConfiguration {
        @Bean("tisRepository")
        public static @NotNull GitRepository tisRepository(GitProperties gitProperties) {
            return TestConfigurationKt.readOnlyLocalClone("../tis100/leaderboard", gitProperties);
        }
    }

    static final boolean noop = false;

    static final int deleteMin = 1;
    static final int deleteMax = 0;
    static final List<String> replaceStrs = List.of("");

    static final int swapSizeMin = 1;
    static final int swapSizeMax = 0;
    static final int swapDistMin = 1;
    static final int swapDistMax = 0;
    static final boolean swapMoveLabel = true;

    static final int dupeMin = 1;
    static final int dupeMax = 0;
    static final int dupeDistanceMin = 0;
    static final int dupeDistanceMax = 0;
    static final boolean dupeLabelTop = false;

    static final List<TISOperation> jumpReplacerNew = List.of(JMP, JEZ, JNZ, JGZ, JLZ).subList(0, 0);
    static final Set<TISOperation> jumpReplacerIgnore = EnumSet.of(HCF, NOP, SWP, SAV, NEG, MOV, ADD, SUB, JMP, JEZ, JGZ, JNZ, JLZ, JRO);
    static final boolean jumpReplaceNotPrepend = true;

    static final int labelShiftMin = 1; // move labels down by this amount one by one
    static final int labelShiftMax = 0; // move labels down by this amount one by one

    static final boolean fuseMovMovAcc = false; // fuse MOV X ACC; MOV ACC Y into MOV X Y
    static final boolean fuseArith = false; // fuse ADD imm1; ADD imm2 into ADD [imm1+imm2]

    static final boolean movAddSwap = false; // swap MOV X ACC; ADD Y with MOV Y ACC; ADD X
    static final boolean jmpExchange = false; // exchange JEZ A; JMP B with JNZ B; JMP A and viceversa
    static final boolean jmpExchange2 = false; // exchange JEZ A; JLZ A with JGZ NEXT; JMP A and similar

    static final List<TISPort> srcReplacements = List.of(NIL, LAST, LEFT, RIGHT, UP, DOWN, ANY, ACC, IMMEDIATE).subList(0, 0);
    static final short srcImmReplacement = 0;
    static final List<TISPort> dstReplacements = List.of(NIL, LAST, LEFT, RIGHT, UP, DOWN, ANY, ACC).subList(0, 0);

    static final int fuzzFixed = 0; // move immediates around by up to this value
    static final boolean fuzzAll = false;

    static final int fuzzTries = 0; // random fuzz tries
    static final double fuzzWindow = 1.2;
    static final int fuzzDelta = 3;

    static final String syntheticLabel = "&&";
    static final TISPuzzle startingPuzzle = TISPuzzle.SIGNAL_AMPLIFIER;
    static final Path solutionsPath = Paths.get("/tmp/macina");
    static final Path dumpPath = false ? Paths.get("/tmp/dels11") : null;

    @Test
    public void theAIRevolution() throws IOException {
        /*
        rm -rf /tmp/macina; mkdir -p /tmp/macina
        git ls-files --others --exclude-standard | xargs -I% cp % /tmp/macina
        git diff @~20 --no-renames --diff-filter=A --name-only | xargs -I% cp % /tmp/macina
         */
        if (dumpPath != null)
            Files.createDirectory(dumpPath);

        for (TISPuzzle puzzle : repository.getTrackedPuzzles()) {
            if (startingPuzzle != null && puzzle.ordinal() < startingPuzzle.ordinal()) continue;
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(solutionsPath, puzzle.getId() + "*.txt")) {
                for (Path path : paths) {
                    TISSave save;
                    try {
                        save = TISSave.unmarshal(path);
                    }
                    catch (ValidationException e) {
                        System.err.println(path + ": " + e.getMessage());
                        continue;
                    }
                    String baseDescr = path.getFileName().toString()
                                           .replace(puzzle.getId() + ".", "").replace(".txt", "");
                    if (noop) {
                        submit(Files.readString(path), puzzle, baseDescr, null);
                    }
                    for (String replaceStr: replaceStrs) {
                        for (int delAmount = deleteMin; delAmount <= deleteMax; delAmount++) {
                            genReplace(save, puzzle, baseDescr, delAmount, replaceStr);
                        }
                    }
                    for (int swapSize = swapSizeMin; swapSize <= swapSizeMax; swapSize++) {
                        for (int swapDist = swapDistMin; swapDist <= swapDistMax; swapDist++) {
                            genSwap(save, puzzle, baseDescr, swapSize, swapDist);
                        }
                    }
                    for (int dupeAmount = dupeMin; dupeAmount <= dupeMax; dupeAmount++) {
                        for (int dupeDistance = dupeDistanceMin; dupeDistance <= dupeDistanceMax; dupeDistance++) {
                            genDupe(save, puzzle, baseDescr, dupeAmount, dupeDistance);
                        }
                    }
                    for (TISOperation newJump : jumpReplacerNew) {
                        genJumpReplacer(save, puzzle, baseDescr, newJump);
                    }
                    for (int labelShift = labelShiftMin; labelShift <= labelShiftMax; labelShift++) {
                        genLabelShift(save, puzzle, baseDescr, labelShift);
                    }
                    if (fuseMovMovAcc) {
                        genFuseMovMovAcc(save, puzzle, baseDescr);
                    }
                    if (fuseArith) {
                        genFuseArith(save, puzzle, baseDescr);
                    }
                    if (movAddSwap) {
                        genMovAddSwap(save, puzzle, baseDescr);
                    }
                    if (jmpExchange) {
                        genJmpExchange(save, puzzle, baseDescr);
                    }
                    if (jmpExchange2) {
                        genJmpExchange2(save, puzzle, baseDescr);
                    }
                    for (TISPort newDst : srcReplacements) {
                        genReplaceSrc(save, puzzle, baseDescr, newDst);
                    }
                    for (TISPort newDst : dstReplacements) {
                        genReplaceDst(save, puzzle, baseDescr, newDst);
                    }
                    for (int fuzzFactor = -fuzzFixed; fuzzFactor <= fuzzFixed; fuzzFactor++) {
                        genFuzzFixed(save, puzzle, baseDescr, fuzzFactor);
                    }
                    if (fuzzTries != 0) {
                        genFuzzTries(save, puzzle, baseDescr);
                    }
                }
            }
            System.out.printf("Done %-14s %s\n", puzzle.getId(), puzzle.getDisplayName());
        }

        /*
        rsync -a --delete --exclude=README.txt $(ls -1dt /tmp/leaderboard* | head -n1)/* ../tis100/leaderboard/
        rm -f /tmp/p/*; git ls-files --others --exclude-standard | xargs -I% cp % /tmp/p
        for f in $(git ls-files --others --exclude-standard); do TISsubmit $f; done
        git clean -xf && git fetch && git reset --hard origin/master
         */

        System.out.println("Done " + tries + " tries, " + valids + " validated, " + updates + " updates, " + successes + " successes");
    }

    private void genReplace(@NotNull TISSave save, TISPuzzle puzzle, String baseDescr, int delAmount, String replaceStr) {
        for (int delStart = 0; delStart + delAmount - 1 < save.getInstructions(); delStart++) {
            int codeCount = 0;
            StringBuilder data = new StringBuilder();
            for (TISNode node : save.getNodes()) {
                data.append('@').append(node.getId()).append('\n');
                for (TISLine line : node.getLines()) {
                    if (line.isCode()) {
                        codeCount++;
                        if (codeCount >= delStart + 1 && codeCount <= delStart + delAmount) {
                            if (codeCount < delStart + delAmount || replaceStr.isEmpty()) {
                                if (line.getLabel() != null)
                                    data.append(line.getLabel()).append(":\n");
                            }
                            else {
                                writeLabel(data, line);
                                data.append(replaceStr).append(" #G\n");
                            }
                            continue;
                        }
                    }
                    data.append(line.getRawLine()).append('\n');
                }
            }
            String descr = baseDescr + "?d" + delStart + "-" + (delStart + delAmount - 1) +
                           (replaceStr.isEmpty() ? "" : "->" + replaceStr);
            submit(data, puzzle, descr, null);
        }
    }

    private void genSwap(@NotNull TISSave save, TISPuzzle puzzle, String baseDescr, int swapSize, int swapDist) {
        for (int codeLineIdx = 0; codeLineIdx + swapSize + swapDist < save.getInstructions(); codeLineIdx++) {
            int codeCount = 0;
            List<TISLine> swappedLines = new ArrayList<>();
            StringBuilder data = new StringBuilder();
            for (TISNode node : save.getNodes()) {
                data.append('@').append(node.getId()).append('\n');
                for (TISLine line : node.getLines()) {
                    if (line.isCode()) {
                        codeCount++;
                        if (codeLineIdx + 1 <= codeCount && codeCount <= codeLineIdx + swapSize) {
                            swappedLines.add(line);
                            if (!swapMoveLabel)
                                writeLabel(data, line);
                            continue;
                        }
                        data.append(line.getRawLine()).append('\n');
                        if (codeCount == codeLineIdx + swapSize + swapDist) {
                            for (TISLine swappedLine : swappedLines) {
                                if (swapMoveLabel)
                                    data.append(swappedLine.getRawLine());
                                else
                                    data.append(swappedLine.code());
                                data.append('\n');
                            }
                        }
                    }
                    else {
                        data.append(line.getRawLine()).append('\n');
                    }
                }
            }
            String descr = baseDescr + "?s" + codeLineIdx + "-" + (codeLineIdx + swapSize) + "+" + swapDist;
            submit(data, puzzle, descr, null);
        }
    }

    private void genDupe(@NotNull TISSave save, TISPuzzle puzzle, String baseDescr, int dupeAmount, int dupeDistance) {
        for (int dupeStart = 0; dupeStart + dupeAmount + dupeDistance - 1 < save.getInstructions(); dupeStart++) {
            int codeCount = 0;
            StringBuilder data = new StringBuilder();
            StringBuilder dupe = new StringBuilder();
            for (TISNode node : save.getNodes()) {
                data.append('@').append(node.getId()).append('\n');
                for (TISLine line : node.getLines()) {
                    if (line.isCode()) {
                        codeCount++;
                        if (dupeStart + 1 <= codeCount && codeCount <= dupeStart + dupeAmount + dupeDistance) {
                            if (codeCount <= dupeStart + dupeAmount) {
                                String dupeLine = line.code() + " #D";
                                dupe.append(dupeLabelTop ? dupeLine : line.getRawLine()).append('\n');
                                data.append(dupeLabelTop ? line.getRawLine() : dupeLine).append('\n');
                            }
                            else {
                                data.append(line.getRawLine()).append('\n');
                            }
                            if (codeCount == dupeStart + dupeAmount + dupeDistance) {
                                data.append(dupe);
                            }
                            continue;
                        }
                    }
                    data.append(line.getRawLine()).append('\n');
                }
            }
            String descr = baseDescr + "?DU" + dupeAmount + "L" + dupeStart + "D" + dupeDistance;
            submit(data, puzzle, descr, null);
        }
    }

    private void genJumpReplacer(@NotNull TISSave save, TISPuzzle puzzle, String baseDescr, TISOperation newJump) {
        for (int jumpPoint = 0; jumpPoint < TISSave.MAX_NODE_INSTR; jumpPoint++) {
            int foundLine = 0;
            boolean found = true;
            while (found) {
                found = false;
                int countLine = 0;
                StringBuilder data = new StringBuilder();
                for (TISNode node : save.getNodes()) {
                    data.append('@').append(node.getId()).append('\n');
                    int codeIdx = 0;
                    List<TISLine> codeLines = node.getCodeLines();
                    for (TISLine line : node.getLines()) {
                        if (line.isCode()) {
                            int off = jumpPoint - codeIdx; // jro equiv
                            if (off == 0 && line.getLabel() == null)
                                data.append(syntheticLabel + ": ");
                            codeIdx++;
                            if (off != 0 && off != -1 &&
                                !jumpReplacerIgnore.contains(line.getOp()) &&
                                jumpPoint < codeLines.size() &&
                                (jumpReplaceNotPrepend || codeLines.size() < TISSave.MAX_NODE_INSTR)) {
                                countLine++;
                                if (found || countLine <= foundLine) {
                                    //
                                }
                                else {
                                    if (jumpReplaceNotPrepend)
                                        writeLabel(data, line);
                                    data.append(newJump).append(" ");
                                    TISLine destLine = codeLines.get(jumpPoint);
                                    if (destLine.getLabel() != null)
                                        data.append(destLine.getLabel()).append(" #J");
                                    else
                                        data.append(syntheticLabel);
                                    data.append('\n');
                                    foundLine = countLine;
                                    found = true;
                                    if (jumpReplaceNotPrepend)
                                        continue;
                                }
                            }
                        }
                        data.append(line.getRawLine()).append('\n');
                    }
                }
                if (found) {
                    String descr = baseDescr + "?" + newJump + jumpPoint + "L" + foundLine;
                    submit(data, puzzle, descr, null);
                }
            }
        }
    }

    private void genLabelShift(TISSave save, TISPuzzle puzzle, String baseDescr, int labelShift) {
        int foundLine = 0;
        boolean found = true;
        while (found) {
            found = false;
            String foundLabel = null;
            int countLine = 0;
            StringBuilder data = new StringBuilder();
            for (TISNode node : save.getNodes()) {
                data.append('@').append(node.getId()).append('\n');
                for (TISLine line : node.getLines()) {
                    countLine++;
                    if (found || countLine <= foundLine) {
                        //
                    }
                    else if (foundLabel != null && line.isCode() && countLine >= foundLine + labelShift) {
                        found = true;
                        data.append(foundLabel).append(": ");
                    }
                    else if (line.getLabel() != null) {
                        if (line.isCode())
                            data.append(line.code());
                        data.append('\n');
                        foundLabel = line.getLabel();
                        foundLine = countLine;
                        continue;
                    }

                    data.append(line.getRawLine()).append('\n');
                }
            }
            if (found) {
                String descr = baseDescr + "?SL" + labelShift + "L" + foundLine;
                submit(data, puzzle, descr, null);
            }
        }
    }

    private void genFuseMovMovAcc(TISSave save, TISPuzzle puzzle, String baseDescr) {
        int foundLine = 0;
        boolean found = true;
        while (found) {
            found = false;
            int countLine = 0;
            StringBuilder data = new StringBuilder();
            for (TISNode node : save.getNodes()) {
                data.append('@').append(node.getId()).append('\n');
                @NotNull List<TISLine> lines = node.getLines();
                linesLoop:
                for (int i = 0; i < lines.size(); i++) {
                    TISLine line = lines.get(i);
                    countLine++;
                    if (found || countLine <= foundLine) {
                        //
                    }
                    else if (line.is(MOV, null, ACC)) {
                        for (int j = i + 1; j < lines.size(); j++) {
                            TISLine nextLine = lines.get(j);
                            if (nextLine.isCode()) {
                                if (nextLine.is(MOV, ACC, null) || nextLine.is(JRO, ACC)) {
                                    // collapse
                                    writeLabel(data, line);
                                    writeLabel(data, nextLine);
                                    data.append(nextLine.getOp()).append(" ").append(line.arg1());
                                    if (nextLine.is(MOV))
                                        data.append(" ").append(nextLine.arg2());
                                    data.append('\n');
                                    found = true;
                                    foundLine = countLine;
                                    i = j;
                                    continue linesLoop;
                                }
                                break;
                            }
                        }
                    }
                    data.append(line.getRawLine()).append('\n');
                }
            }
            if (found) {
                String descr = baseDescr + "?FMM" + foundLine;
                submit(data, puzzle, descr, null);
            }
        }
    }

    private void genFuseArith(TISSave save, TISPuzzle puzzle, String baseDescr) {
        int foundLine = 0;
        boolean found = true;
        while (found) {
            found = false;
            int countLine = 0;
            StringBuilder data = new StringBuilder();
            for (TISNode node : save.getNodes()) {
                data.append('@').append(node.getId()).append('\n');
                @NotNull List<TISLine> lines = node.getLines();
                linesLoop:
                for (int i = 0; i < lines.size(); i++) {
                    TISLine line = lines.get(i);
                    countLine++;
                    if (found || countLine <= foundLine) {
                        //
                    }
                    else if (line.is(ADD, IMMEDIATE) || line.is(SUB, IMMEDIATE)) {
                        for (int j = i + 1; j < lines.size(); j++) {
                            TISLine nextLine = lines.get(j);
                            if (nextLine.isCode()) {
                                if (nextLine.is(ADD, IMMEDIATE) || nextLine.is(SUB, IMMEDIATE)) {
                                    // collapse
                                    writeLabel(data, line);
                                    writeLabel(data, nextLine);
                                    int imm = (line.is(ADD) ? line.getImmediate() : -line.getImmediate()) +
                                              (nextLine.is(ADD) ? nextLine.getImmediate() : -nextLine.getImmediate());
                                    data.append("ADD ").append(imm).append('\n');
                                    found = true;
                                    foundLine = countLine;
                                    i = j;
                                    continue linesLoop;
                                }
                                break;
                            }
                        }
                    }
                    data.append(line.getRawLine()).append('\n');
                }
            }
            if (found) {
                String descr = baseDescr + "?FAR" + foundLine;
                submit(data, puzzle, descr, null);
            }
        }
    }

    private void genMovAddSwap(TISSave save, TISPuzzle puzzle, String baseDescr) {
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
                        if (found || countLine <= foundLine) {
                            //
                        }
                        else if (movLine != null) {
                            if (line.isAny(ADD, SUB) && line.getPort1() != ACC) {
                                writeLabel(data, movLine);
                                String movArg =
                                    line.is(SUB, IMMEDIATE) ? String.valueOf(-line.getImmediate()) : line.arg1();
                                data.append("MOV ").append(movArg).append(" ACC").append('\n');
                                if (line.is(SUB) && line.getPort1() != IMMEDIATE)
                                    data.append("NEG\n");
                                writeLabel(data, line);
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
                String descr = baseDescr + "?MA" + foundLine;
                submit(data, puzzle, descr, null);
            }
        }
    }

    private void genJmpExchange(TISSave save, TISPuzzle puzzle, String baseDescr) {
        int foundLine = 0;
        boolean found = true;
        while (found) {
            found = false;
            int countLine = 0;
            StringBuilder data = new StringBuilder();
            for (TISNode node : save.getNodes()) {
                data.append('@').append(node.getId()).append('\n');
                @NotNull List<TISLine> lines = node.getLines();
                linesLoop:
                for (int i = 0; i < lines.size(); i++) {
                    TISLine line = lines.get(i);
                    if (line.isCode()) {
                        countLine++;
                        if (found || countLine <= foundLine) {
                            //
                        }
                        else if (line.isAny(JEZ, JNZ, JGZ, JLZ)) {
                            for (int j = i + 1; j < lines.size(); j++) {
                                TISLine nextLine = lines.get(j);
                                if (nextLine.isCode()) {
                                    if (nextLine.is(JMP)) {
                                        String inverted = switch (line.getOp()) {
                                            case JEZ -> "JNZ ";
                                            case JNZ -> "JEZ ";
                                            case JGZ -> "JLZ "; // lossy
                                            case JLZ -> "JGZ "; // lossy
                                            default -> throw new IllegalStateException();
                                        };
                                        writeLabel(data, line);
                                        data.append(inverted).append(nextLine.getJumpTarget()).append('\n');
                                        writeLabel(data, nextLine);
                                        data.append("JMP ").append(line.getJumpTarget()).append('\n');
                                        i = j;
                                        foundLine = countLine;
                                        found = true;
                                        continue linesLoop;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    data.append(line.getRawLine()).append('\n');
                }
            }
            if (found) {
                String descr = baseDescr + "?EX" + foundLine;
                submit(data, puzzle, descr, null);
            }
        }
    }

    private void genJmpExchange2(TISSave save, TISPuzzle puzzle, String baseDescr) {
        int foundLine = 0;
        boolean found = true;
        while (found) {
            found = false;
            int countLine = 0;
            StringBuilder data = new StringBuilder();
            for (TISNode node : save.getNodes()) {
                data.append('@').append(node.getId()).append('\n');
                @NotNull List<TISLine> lines = node.getLines();
                linesLoop:
                for (int i = 0; i < lines.size(); i++) {
                    TISLine line = lines.get(i);
                    if (line.isCode()) {
                        countLine++;
                        if (found || countLine <= foundLine) {
                            //
                        }
                        else if (line.isAny(JEZ, JGZ, JLZ)) {
                            for (int j = i + 1; j < lines.size(); j++) {
                                TISLine nextLine = lines.get(j);
                                if (nextLine.isCode()) {
                                    if (nextLine.isAny(JEZ, JGZ, JLZ) && line.getOp() != nextLine.getOp() &&
                                        line.getJumpTarget().equals(nextLine.getJumpTarget())) {
                                        int missingOrd = JEZ.ordinal() + JGZ.ordinal() + JLZ.ordinal() -
                                                         line.getOp().ordinal() - nextLine.getOp().ordinal();
                                        TISOperation missing = TISOperation.values()[missingOrd];
                                        writeLabel(data, line);
                                        data.append(missing.name()).append(' ').append(syntheticLabel).append('\n');
                                        writeLabel(data, nextLine);
                                        data.append("JMP ").append(line.getJumpTarget()).append('\n');
                                        data.append(syntheticLabel).append(": ");
                                        i = j;
                                        foundLine = countLine;
                                        found = true;
                                        continue linesLoop;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    data.append(line.getRawLine()).append('\n');
                }
            }
            if (found) {
                String descr = baseDescr + "?EX" + foundLine;
                submit(data, puzzle, descr, null);
            }
        }
    }

    private void genReplaceSrc(TISSave save, TISPuzzle puzzle, String baseDescr, TISPort newSrc) {
        String newArg = newSrc == IMMEDIATE ? Short.toString(srcImmReplacement) : newSrc.name();
        EnumSet<TISPort> invalidPorts = EnumSet.of(NIL, LABEL, newSrc);
        int foundLine = 0;
        boolean found = true;
        while (found) {
            found = false;
            int countLine = 0;
            StringBuilder data = new StringBuilder();
            for (TISNode node : save.getNodes()) {
                data.append('@').append(node.getId()).append('\n');
                for (TISLine line : node.getLines()) {
                    if (line.getPort1() != null && !invalidPorts.contains(line.getPort1())) {
                        countLine++;
                        if (found || countLine <= foundLine) {
                            //
                        }
                        else {
                            data.append(line.getRawLine().replace(line.arg1(), newArg)).append('\n');
                            foundLine = countLine;
                            found = true;
                            continue;
                        }
                    }
                    data.append(line.getRawLine()).append('\n');
                }
            }
            if (found) {
                String descr = baseDescr + "?RS" + foundLine;
                submit(data, puzzle, descr, null);
            }
        }
    }

    private void genReplaceDst(TISSave save, TISPuzzle puzzle, String baseDescr, TISPort newDst) {
        int foundLine = 0;
        boolean found = true;
        while (found) {
            found = false;
            int countLine = 0;
            StringBuilder data = new StringBuilder();
            for (TISNode node : save.getNodes()) {
                data.append('@').append(node.getId()).append('\n');
                for (TISLine line : node.getLines()) {
                    if (line.is(MOV) && line.getPort2() != NIL && line.getPort2() != newDst) {
                        countLine++;
                        if (found || countLine <= foundLine) {
                            //
                        }
                        else {
                            writeLabel(data, line);
                            data.append("MOV ").append(line.arg1()).append(" ").append(newDst.name()).append('\n');
                            foundLine = countLine;
                            found = true;
                            continue;
                        }
                    }
                    data.append(line.getRawLine()).append('\n');
                }
            }
            if (found) {
                String descr = baseDescr + "?RD" + foundLine;
                submit(data, puzzle, descr, null);
            }
        }
    }

    private void genFuzzFixed(TISSave save, TISPuzzle puzzle, String baseDescr, int fuzzFactor) {
        if (fuzzFactor == 0)
            return;
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
                        if (!fuzzAll && (found || countLine <= foundLine)) {
                            //
                        }
                        else {
                            String fuzzedLine = line.getRawLine()
                                                    .replace(String.valueOf(line.getImmediate()),
                                                             String.valueOf(line.getImmediate() + fuzzFactor));
                            data.append(fuzzedLine).append('\n');
                            foundLine = countLine;
                            found = true;
                            continue;
                        }
                    }
                    data.append(line.getRawLine()).append('\n');
                }
            }
            if (found) {
                String descr = baseDescr + "?F" + fuzzFactor + (fuzzAll ? "" : "L" + foundLine);
                submit(data, puzzle, descr, null);
            }
            if (fuzzAll)
                break;
        }
    }

    private void genFuzzTries(@NotNull TISSave save, TISPuzzle puzzle, String baseDescr) {
        SplittableRandom rng = new SplittableRandom();
        long nums = save.codeAsStream()
                        .filter(TISAIRevolutionTest::hasFuzzableArg).count();
        for (int i = 0; i < fuzzTries * nums; i++) {
            StringBuilder data = new StringBuilder();
            for (TISNode node : save.getNodes()) {
                data.append('@').append(node.getId()).append('\n');
                for (TISLine line : node.getLines()) {
                    if (hasFuzzableArg(line)) {
                        int a = Math.abs(line.getImmediate());
                        int val = rng.nextInt((int) (a / fuzzWindow - fuzzDelta), (int) (a * fuzzWindow + fuzzDelta));
                        if (line.getImmediate() < 0) val *= -1;
                        String fuzzedLine = line.getRawLine().replace(String.valueOf(line.getImmediate()),
                                                                      String.valueOf(val));
                        data.append(fuzzedLine).append('\n');
                    }
                    else
                        data.append(line.getRawLine()).append('\n');
                }
            }
            String descr = baseDescr + "?FD" + fuzzDelta + "T" + i;
            submit(data, puzzle, descr, null);
        }
    }

    private static boolean hasFuzzableArg(@NotNull TISLine line) {
        return line.is(ADD, IMMEDIATE) || line.is(SUB, IMMEDIATE) || line.is(MOV, IMMEDIATE, ACC);
//        return line.getPort1() == IMMEDIATE;
    }

    private static void writeLabel(@NotNull StringBuilder data, @NotNull TISLine line) {
        if (line.getLabel() != null)
            data.append(line.getLabel()).append(": ");
    }

    private static int tries = 0;
    private static int valids = 0;
    private static int successes = 0;
    private static int updates = 0;
    private static final EnumMap<TISPuzzle, TIntSet> dumpHashes = new EnumMap<>(TISPuzzle.class);

    @SneakyThrows
    private void submit(@NotNull CharSequence data, @NotNull TISPuzzle puzzle, String descr, String link) {
        tries++;
        if (dumpPath != null) {
            boolean isNew = dumpHashes.computeIfAbsent(puzzle, k -> new TIntHashSet()).add(data.hashCode());
            if (isNew)
                Files.writeString(dumpPath.resolve(puzzle.getId() + "-" + descr + ".txt"), data);
        }
        else {
            TISSubmission submission;
            try {
                submission = TISSubmission.fromData(data.toString(), puzzle, descr, link);
            }
            catch (ValidationException e) {
                return;
            }
            valids++;
            SubmitResult<TISRecord, TISCategory> result = repository.submit(submission);
            if (result instanceof SubmitResult.Success<TISRecord, TISCategory>) {
                successes++;
                System.out.println(result);
            }
            else if (result instanceof SubmitResult.Updated<TISRecord, TISCategory>) {
                updates++;
                System.out.println(result);
            }
        }
    }
}