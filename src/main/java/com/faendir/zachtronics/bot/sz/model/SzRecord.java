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

package com.faendir.zachtronics.bot.sz.model;

import com.faendir.zachtronics.bot.model.Record;
import kotlin.Pair;
import lombok.SneakyThrows;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

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
    public List<Pair<String, InputStream>> attachments() {
        return Collections.singletonList(new Pair<>(link.substring(link.lastIndexOf('/')), new URL(link).openStream()));
    }
}