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

package com.faendir.zachtronics.bot.inf.rest.dto;

import com.faendir.zachtronics.bot.inf.model.IfCategory;
import com.faendir.zachtronics.bot.inf.model.IfRecord;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.rest.dto.RecordDTO;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@Value
public class IfRecordDTO implements RecordDTO<IfScoreDTO> {
    @NotNull IfScoreDTO score;
    @NotNull String fullFormattedScore;
    @NotNull String author;
    @Nullable String gif = null;
    @Nullable String solution;
    @Nullable String smartFormattedCategories;

    @NotNull
    public static IfRecordDTO fromCategoryRecord(@NotNull CategoryRecord<IfRecord, IfCategory> categoryRecord) {
        IfRecord record = categoryRecord.getRecord();
        Set<IfCategory> categories = categoryRecord.getCategories();
        return new IfRecordDTO(
                IfScoreDTO.fromScore(record.getScore()),
                record.getScore().toDisplayString(DisplayContext.plainText()),
                record.getAuthor(),
                record.getDataLink(),
                UtilsKt.smartFormat(categories, UtilsKt.toMetricsTree(record.getPuzzle().getSupportedCategories()))
        );
    }

    @NotNull
    public static IfRecordDTO fromRecord(@NotNull IfRecord record) {
        return new IfRecordDTO(IfScoreDTO.fromScore(record.getScore()),
                               record.getScore().toDisplayString(DisplayContext.plainText()),
                               record.getAuthor(),
                               record.getDataLink(),
                               null);
    }
}
