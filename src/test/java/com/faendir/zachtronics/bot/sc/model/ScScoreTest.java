package com.faendir.zachtronics.bot.sc.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScScoreTest {
    @Test
    public void testParse() {
        assertEquals(new ScScore(123, 4, 56, false, false), ScScore.parseBPScore("123/4/56"));
        assertEquals(new ScScore(123, 4, 56, true, false), ScScore.parseBPScore("123-4-56-B"));
        assertEquals(new ScScore(123, 4, 56, false, true), ScScore.parseBPScore("123/4-56/P"));
        assertEquals(new ScScore(123, 4, 56, true, true), ScScore.parseBPScore("123-4/56-BP"));
    }
}