package com.faendir.zachtronics.bot.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {

    @Test
    public void testRawPastebin() {
        assertTransform("https://pastebin.com/raw/WfFFDhBA", "https://pastebin.com/WfFFDhBA");
        assertTransform("https://pastebin.com/raw/WfFFDhBA", "https://pastebin.com/raw/WfFFDhBA");
        assertTransform("https://pastebin.com/raw/WfFFDhBA", "https://pastebin.com/dl/WfFFDhBA");
    }

    @Test
    public void testRawGDocs() {
        assertTransform(
                "https://docs.google.com/document/d/1V2mhFdfx5zw3_8gNwE9YnZhDCS23mQSnlWq--pFVZa4/export?format=txt",
                "https://docs.google.com/document/d/1V2mhFdfx5zw3_8gNwE9YnZhDCS23mQSnlWq--pFVZa4/edit?usp=sharing");
        assertTransform(
                "https://docs.google.com/document/d/15bc2nNiVpRksI7pm0Z5_UtK9r1wJ3yg7GwCXakHGm3k/export?format=txt",
                "https://docs.google.com/document/d/15bc2nNiVpRksI7pm0Z5_UtK9r1wJ3yg7GwCXakHGm3k/edit?usp=sharing");
    }

    private static void assertTransform(String output, String input) {
        assertEquals(output, Utils.rawContentURL(input));
    }
}