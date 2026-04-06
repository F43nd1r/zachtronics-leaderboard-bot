/*
 * Copyright (c) 2025
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

package com.faendir.zachtronics.bot.kz.rest.dto;

import com.faendir.zachtronics.bot.kz.model.KzCategory;
import com.faendir.zachtronics.bot.kz.model.KzRecord;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.rest.dto.RecordDTO;
import com.faendir.zachtronics.bot.utils.MetricsTreeKt;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@Value
public class KzRecordDTO implements RecordDTO<KzScoreDTO> {
    @NonNull KzScoreDTO score;
    @NonNull String fullFormattedScore;
    @NonNull String author;
    @Nullable String gif;
    @Nullable String solution;
    @Nullable String smartFormattedCategories;

    @NonNull
    public static KzRecordDTO fromCategoryRecord(@NonNull CategoryRecord<KzRecord, KzCategory> categoryRecord) {
        KzRecord record = categoryRecord.getRecord();
        Set<KzCategory> categories = categoryRecord.getCategories();
        return new KzRecordDTO(
                KzScoreDTO.fromScore(record.getScore()),
                record.getScore().toDisplayString(DisplayContext.plainText()),
                record.getAuthor(),
                record.getDisplayLink(),
                record.getDataLink(),
                MetricsTreeKt.smartFormat(categories, record.getPuzzle().getSupportedCategories())
        );
    }

    @NonNull
    public static KzRecordDTO fromRecord(@NonNull KzRecord record) {
        return new KzRecordDTO(KzScoreDTO.fromScore(record.getScore()),
                               record.getScore().toDisplayString(DisplayContext.plainText()),
                               record.getAuthor(),
                               record.getDisplayLink(),
                               record.getDataLink(),
                               null);
    }
}
