package com.faendir.zachtronics.bot.sz.model;

import com.faendir.zachtronics.bot.model.Record;
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
public class SzRecord implements Record {
    SzScore score;
    String author;
    String link;

    @NotNull
    @Override
    public String toShowDisplayString() {
        return score.toDisplayString() + (author != null ? " by " + author : "");
    }

    @SneakyThrows
    @NotNull
    @Override
    public List<Tuple2<String, InputStream>> attachments() {
        return Collections.singletonList(Tuples.of(link.substring(link.lastIndexOf('/')), new URL(link).openStream()));
    }
}