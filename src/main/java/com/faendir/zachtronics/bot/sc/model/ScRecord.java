package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.Record;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

@Value
@AllArgsConstructor
public class ScRecord implements Record {
    public static final ScRecord IMPOSSIBLE_CATEGORY = new ScRecord(ScScore.INVALID_SCORE, "", "", "", false);

    @NotNull ScScore score;
    @NotNull String author;
    @NotNull String link;
    @NotNull String archiveLink;
    boolean oldVideoRNG;

    @SneakyThrows
    @NotNull
    @Override
    public List<Tuple2<String, InputStream>> attachments() {
        String name = archiveLink.replaceFirst(".+/", "");
        return Collections.singletonList(Tuples.of(name, new URL(archiveLink).openStream()));
    }
}