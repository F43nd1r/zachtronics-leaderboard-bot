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

import Metric from "./Metric"
import RecordDTO, { isStrictlyBetterInMetrics } from "./RecordDTO"
import { Configuration } from "./Configuration"
import iterate from "../utils/iterate"
import Modifier from "./Modifier"

export interface Filter<MODIFIER_ID extends string, METRIC_ID extends string> {
    modifiers?: Record<MODIFIER_ID, boolean | undefined>
    showOnlyFrontier?: boolean
    max?: Record<METRIC_ID, number | undefined>
}

export function applyFilter<MODIFIER_ID extends string, METRIC_ID extends string, SCORE>(
    metrics: Record<METRIC_ID, Metric<SCORE>>,
    modifiers: Record<MODIFIER_ID, Modifier<SCORE>>,
    filter: Filter<MODIFIER_ID, METRIC_ID>,
    configuration: Configuration<METRIC_ID>,
    records: RecordDTO<SCORE>[],
): RecordDTO<SCORE>[] {
    const filteredMetrics = filter.max ? iterate(filter.max).map(([key, max]) => ({ metric: metrics[key], max: max })) : []
    const activeMetrics = (configuration.mode === "2D" ? [configuration.x, configuration.y] : [configuration.x, configuration.y, configuration.z]).map((id) => metrics[id])
    const filteredRecords = records.filter(
        (record: RecordDTO<SCORE>) =>
            (!filter.modifiers || iterate(filter.modifiers).every(([modifierId, value]) => value === undefined || value === modifiers[modifierId].get(record.score))) &&
            !filteredMetrics.some(({ metric, max }) => {
                const value = metric.get(record.score)
                return max !== undefined && (value === undefined || value > max)
            }),
    )
    return filteredRecords.filter((record: RecordDTO<SCORE>) => !filter.showOnlyFrontier || !filteredRecords.some((r) => isStrictlyBetterInMetrics(r, record, activeMetrics)))
}
