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

package com.faendir.zachtronics.bot.om.rest.dto

import com.faendir.zachtronics.bot.om.model.OmMetric

data class OmMetricDTO(val id: String, val displayName: String, val description: String, val type: String)

fun OmMetric<*>.toDTO() = OmMetricDTO(
    id = this::class.simpleName!!,
    displayName = displayName,
    description = description,
    type = when(this) {
        is OmMetric.Value<*> -> "VALUE"
        is OmMetric.Modifier -> "MODIFIER"
        is OmMetric.Computed<*> -> "COMPUTED"
        is OmMetric.Constant<*> -> "CONSTANT"
    }
)
