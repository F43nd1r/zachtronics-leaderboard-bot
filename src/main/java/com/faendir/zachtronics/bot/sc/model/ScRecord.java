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
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

    @NotNull
    @Override
    public String toEmbedDisplayString() {
        return toLbDisplayString("%s%s/%d/%d", "");
    }

    /**
     * @param formatString there needs to be <tt>%s%s/%d/%d</tt>, more formatting can be added on top
     * @param reactorPrefix will be put between the archive and the video link
     */
    @NotNull
    public String toLbDisplayString(String formatString, String reactorPrefix) {
        String cyclesStr =
                score.getCycles() >= 100000 ? NumberFormat.getNumberInstance(Locale.ROOT).format(score.getCycles())
                                            : Integer.toString(score.getCycles());
        return String.format("[\uD83D\uDCC4](%s) %s[(" + formatString + "%s) %s](%s)", // 📄
                             archiveLink, reactorPrefix, cyclesStr,
                             oldVideoRNG ? "\\*" : "", score.getReactors(), score.getSymbols(),
                             score.slashFlags(), author, link);
    }
}