/*
 * Copyright (c) 2022
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

package com.faendir.zachtronics.bot.cw.rest.dto;

import com.faendir.zachtronics.bot.cw.model.CwCategory;
import com.faendir.zachtronics.bot.cw.model.CwRecord;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.rest.dto.RecordDTO;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@Value
public class CwRecordDTO implements RecordDTO<CwScoreDTO> {
    @NotNull CwScoreDTO score;
    @NotNull String fullFormattedScore;
    @NotNull String author;
    @Nullable String gif;
    @Nullable String solution;
    @Nullable String smartFormattedCategories;

    @NotNull
    public static CwRecordDTO fromCategoryRecord(@NotNull CategoryRecord<CwRecord, CwCategory> categoryRecord) {
        CwRecord record = categoryRecord.getRecord();
        Set<CwCategory> categories = categoryRecord.getCategories();
        return new CwRecordDTO(
                CwScoreDTO.fromScore(record.getScore()),
                record.getScore().toDisplayString(DisplayContext.plainText()),
                record.getAuthor(),
                record.getDisplayLink(),
                record.getDataLink(),
                UtilsKt.smartFormat(categories, UtilsKt.toMetricsTree(record.getPuzzle().getSupportedCategories()))
        );
    }

    @NotNull
    public static CwRecordDTO fromRecord(@NotNull CwRecord record) {
        return new CwRecordDTO(CwScoreDTO.fromScore(record.getScore()),
                               record.getScore().toDisplayString(DisplayContext.plainText()),
                               record.getAuthor(),
                               record.getDisplayLink(),
                               record.getDataLink(),
                               null);
    }
}
