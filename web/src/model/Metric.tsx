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

import OmRecord from "./Record"

export type MetricId = "g" | "c" | "a" | "i" | "h" | "w" | "r" | "t" | "o"

export interface Metric {
    id: MetricId
    name: string
    get: (record: OmRecord) => number | undefined
}

export const metrics: Metric[] = [
    {
        id: "g",
        name: "Cost",
        get(record) {
            return record.score?.cost
        },
    },
    {
        id: "c",
        name: "Cycles",
        get(record) {
            return record.score?.cycles
        },
    },
    {
        id: "a",
        name: "Area",
        get(record) {
            return record.score?.area
        },
    },
    {
        id: "i",
        name: "Instructions",
        get(record) {
            return record.score?.instructions
        },
    },
    {
        id: "h",
        name: "Height",
        get(record) {
            return record.score?.height
        },
    },
    {
        id: "w",
        name: "Width",
        get(record) {
            return record.score?.width
        },
    },
    {
        id: "r",
        name: "Rate",
        get(record) {
            return record.score?.rate
        },
    },
]

export function getMetric(id: MetricId) {
    return metrics.find((metric) => metric.id === id)!
}
