/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Disabled("Discord links are ephemeral")
    @Test
    public void testDownloadFilename() {
        String link = "https://cdn.discordapp.com/attachments/281248310126444544/1271707231921897553/NEXUS.14.781.3.0.txt?" +
                      "ex=66b85127&is=66b6ffa7&hm=caf8cd017a68afc109c514c7c10cc16f48605a62d593db87f9b21ec5da1a3e9b&";
        Utils.FileInfo info = Utils.downloadFile(link);
        assertEquals("NEXUS.14.781.3.0.txt", info.getFilename());
        assertTrue(info.dataAsString().contains("MOV RIGHT,DOWN"));
    }

    private static void assertTransform(String output, String input) {
        assertEquals(output, Utils.rawContentURL(input));
    }
}