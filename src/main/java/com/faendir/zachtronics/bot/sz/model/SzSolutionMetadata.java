package com.faendir.zachtronics.bot.sz.model;

import com.faendir.zachtronics.bot.model.Solution;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Value
class SzSolutionMetadata implements Solution {
    String title;
    SzPuzzle puzzle;
    SzScore score;

    /** Closing the stream (if needed) is the caller's issue */
    SzSolutionMetadata(Stream<String> lines) {
        Iterator<String> it = lines.iterator();
        /*
        [name] Top solution Cost->Power - andersk
        [puzzle] Sz040
        [production-cost] 1200
        [power-usage] 679
        [lines-of-code] 31
         */
        title = it.next().replaceFirst("^.+] ", "");
        puzzle = SzPuzzle.valueOf(it.next().replaceFirst("^.+] ", ""));
        int cost = Integer.parseInt(it.next().replaceFirst("^.+] ", "")) / 100;
        int power = Integer.parseInt(it.next().replaceFirst("^.+] ", ""));
        int loc = Integer.parseInt(it.next().replaceFirst("^.+] ", ""));
        score = new SzScore(cost, power, loc);
    }
}
