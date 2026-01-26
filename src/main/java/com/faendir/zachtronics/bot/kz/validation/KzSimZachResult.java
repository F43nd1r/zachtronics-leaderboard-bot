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

package com.faendir.zachtronics.bot.kz.validation;

import lombok.Value;
import org.jetbrains.annotations.Nullable;

/**
 <pre>{"level": 6, "time": 4, "cost": 38, "area": 130, "manipulated": false, "normalized": "base64" }</pre> or
 <pre>{"error": "SolutionIncomplete"}</pre>
 */
@Value
public class KzSimZachResult {
    @Nullable Integer time;
    @Nullable Integer cost;
    @Nullable Integer area;
    @Nullable Integer level;
    @Nullable Boolean manipulated;
    byte @Nullable [] normalized;

    @Nullable String error;
}
