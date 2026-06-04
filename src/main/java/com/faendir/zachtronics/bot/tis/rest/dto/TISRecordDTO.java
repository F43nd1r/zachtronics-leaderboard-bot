/*
 * Copyright (c) 2026
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

package com.faendir.zachtronics.bot.tis.rest.dto;

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.rest.dto.RecordDTO;
import com.faendir.zachtronics.bot.tis.model.TISCategory;
import com.faendir.zachtronics.bot.tis.model.TISRecord;
import com.faendir.zachtronics.bot.utils.MetricsTreeKt;
import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@Value
public class TISRecordDTO implements RecordDTO<TISScoreDTO> {
    TISScoreDTO score;
    String fullFormattedScore;
    String author;
    @Nullable String gif;
    @Nullable String solution;
    @Nullable String smartFormattedCategories;

    public static TISRecordDTO fromCategoryRecord(CategoryRecord<TISRecord, TISCategory> categoryRecord) {
        TISRecord record = categoryRecord.getRecord();
        Set<TISCategory> categories = categoryRecord.getCategories();
        return new TISRecordDTO(
                TISScoreDTO.fromScore(record.getScore()),
                record.getScore().toDisplayString(DisplayContext.plainText()),
                record.getAuthor(),
                record.getDisplayLink(),
                record.getDataLink(),
                MetricsTreeKt.smartFormat(categories, record.getPuzzle().getSupportedCategories())
        );
    }

    public static TISRecordDTO fromRecord(TISRecord record) {
        return new TISRecordDTO(TISScoreDTO.fromScore(record.getScore()),
                               record.getScore().toDisplayString(DisplayContext.plainText()),
                               record.getAuthor(),
                               record.getDisplayLink(),
                               record.getDataLink(),
                               null);
    }
}
