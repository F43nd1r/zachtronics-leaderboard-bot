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

import { getMetric, MetricId } from "./Metric"
import OmRecord, { isStrictlyBetterInMetrics } from "./Record"
import { Configuration } from "./Configuration"

export interface Filter {
    trackless?: boolean
    overlap?: boolean
    showOnlyFrontier?: boolean
    max?: Record<MetricId, number | undefined>
}

export function applyFilter(filter: Filter, configuration: Configuration, records: OmRecord[]): OmRecord[] {
    const filteredMetrics = filter.max ? Object.entries(filter.max).map(([key, max]) => ({ metric: getMetric(key as MetricId), max: max })) : []
    const activeMetrics = (configuration.mode === "2D" ? [configuration.x, configuration.y] : [configuration.x, configuration.y, configuration.z]).map((id) => getMetric(id))
    const filteredRecords = records.filter(
        (record: OmRecord) =>
            (filter.overlap === undefined || filter.overlap === record.score?.overlap) &&
            (filter.trackless === undefined || filter.trackless === record.score?.trackless) &&
            !filteredMetrics.some(({ metric, max }) => {
                const value = metric.get(record)
                return max && (!value || value > max)
            }),
    )
    return filteredRecords.filter((record: OmRecord) => !filter.showOnlyFrontier || !filteredRecords.some((r) => isStrictlyBetterInMetrics(r, record, activeMetrics)))
}
