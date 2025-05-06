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


import com.faendir.zachtronics.bot.exa.model.ExaSubmission;
import com.faendir.zachtronics.bot.validation.ValidationException;
import com.faendir.zachtronics.bot.validation.ValidationUtils;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Wrapper for a Exapt executable installed on the system */
public class Exapt {
    private static final String exaptPath = "../exapunks/exapt/Exapt/bin/Release/net8.0/Exapt";
    private static final String gameDir = "../exapunks/game";

    /**
     * @param untrustedData the score is assumed to be potentially wrong and will be patched
     */
    public static @NotNull ExaSubmission validateData(byte @NotNull [] untrustedData, boolean cheesy, String author, String displayLink) {
        ExaSave save = ExaSave.unmarshal(untrustedData);
        return validateImpl(save, untrustedData, cheesy, author, displayLink);
    }

    /**
     * @param save the score is assumed to be potentially wrong and will be patched
     */
    public static @NotNull ExaSubmission validateSave(@NotNull ExaSave save, boolean cheesy, String author, String displayLink) {
        byte @NotNull [] untrustedData = save.marshal();
        return validateImpl(save, untrustedData, cheesy, author, displayLink);
    }

    static @NotNull ExaSubmission validateImpl(@NotNull ExaSave save, byte @NotNull [] untrustedData, boolean cheesy, String author,
                                               String displayLink) {
        ExaptResult.ExaptStatistics stats = validate(untrustedData);

        byte[] trustedData;
        if (save.getCycles() != stats.getCycles() || save.getSize() != stats.getSize() || save.getActivity() != stats.getActivity()) {
            save.setCycles(stats.getCycles());
            save.setSize(stats.getSize());
            save.setActivity(stats.getActivity());
            trustedData = save.marshal();
        }
        else {
            trustedData = untrustedData;
        }

        return ExaValidator.validateImpl(trustedData, save, cheesy, author, displayLink);
    }

    @SneakyThrows
    static @NotNull ExaptResult.ExaptStatistics validate(byte @NotNull [] data) {
        Path f = Files.createTempFile("exafile", ".solution");
        Files.write(f, data);
        List<String> command = List.of(exaptPath, "-e", gameDir, "-t", "500000", f.toString());
        ExaptResult result = ValidationUtils.callValidator(ExaptResult.class, new byte[0], command);
        Files.delete(f);

        if (!result.isCompleted())
            throw new ValidationException("Exapt: Invalid solution");

        assert result.getStatistics() != null;
        return result.getStatistics();
    }
}
