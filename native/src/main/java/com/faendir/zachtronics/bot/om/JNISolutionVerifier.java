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

import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.net.URL;

public class JNISolutionVerifier {
    static {
        try {
            System.load(ResourceUtils.getFile("classpath:shared/libzachtronicsjni.so").getAbsolutePath());
        } catch (FileNotFoundException f) {
            try {
                URL url = ResourceUtils.getURL("classpath:shared/libzachtronicsjni.so");
                File file = File.createTempFile("libzachtronicsjni", ".so");
                try(InputStream in = url.openStream(); OutputStream out = new FileOutputStream(file)) {
                    StreamUtils.copy(in, out);
                }
                System.load(file.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int getHeight(File puzzle, File solution) {
        return getHeight(puzzle.getAbsolutePath(), solution.getAbsolutePath());
    }

    native int getHeight(String puzzleFile, String solutionFile);

    public int getWidth(File puzzle, File solution) {
        return getWidth(puzzle.getAbsolutePath(), solution.getAbsolutePath());
    }

    native int getWidth(String puzzleFile, String solutionFile);
}
