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
