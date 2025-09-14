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

package com.faendir.zachtronics.bot.validation;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeLoader {

    public static @NotNull SymbolLookup loadLibrary(@NotNull String libName, Arena arena) {
        try {
            return SymbolLookup.libraryLookup(libName, arena);
        }
        catch (IllegalArgumentException ex) {
            String filename = System.mapLibraryName(libName);
            URL url = NativeLoader.class.getClassLoader().getResource("lib/" + filename);
            if (url == null) {
                throw new IllegalArgumentException("Failed to find shared library " + filename);
            }
            try {
                Path lib = Files.createTempFile("load", filename);
                lib.toFile().deleteOnExit();
                try (InputStream in = url.openStream()) {
                    Files.copy(in, lib, StandardCopyOption.REPLACE_EXISTING);
                }
                return SymbolLookup.libraryLookup(lib, arena);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
