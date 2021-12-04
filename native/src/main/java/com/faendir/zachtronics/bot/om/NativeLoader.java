/*
 * Copyright (c) 2021
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

package com.faendir.zachtronics.bot.om;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class NativeLoader {

    public static void loadLibrary(ClassLoader classLoader, String libName) {
        try {
            System.loadLibrary(libName);
        } catch (UnsatisfiedLinkError ex) {
            String filename = "lib" + libName + ".so";
            URL url = classLoader.getResource(filename);
            if (url == null) {
                throw new IllegalArgumentException("Failed to find shared library " + filename);
            }
            try {
                File file = Files.createTempFile("jni", filename).toFile();
                file.deleteOnExit();
                try (InputStream in = url.openStream()) {
                    Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                System.load(file.getCanonicalPath());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

}
