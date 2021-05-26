package com.faendir.zachtronics.bot.sz.model;

import com.faendir.zachtronics.bot.model.Solution;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Getter
public class SzSolution implements Solution {
    @Delegate
    private final SzSolutionMetadata solutionMetadata;

    @Nullable
    private final String content;
    @Nullable
    @Setter
    private Path path;

    public SzSolution(@NotNull String content) {
        solutionMetadata = new SzSolutionMetadata(Pattern.compile("\r?\n").splitAsStream(content));
        this.content = content;
        this.path = null;
    }

    public SzSolution(@NotNull Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            solutionMetadata = new SzSolutionMetadata(lines);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.content = null;
        this.path = path;
    }
}
