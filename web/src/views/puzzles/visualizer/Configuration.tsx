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

import { getMetric, Metric, MetricId } from "../../../model/Metric"
import { Serializer } from "../../../utils/usePersistedState"

export type Mode = "2D" | "3D"

interface RecordFilter {
    trackless?: boolean
    overlap?: boolean
}

export interface Configuration {
    mode: Mode
    filter: RecordFilter
    x: Metric
    y: Metric
    z: Metric
}

interface SerializableConfiguration {
    mode: Mode
    filter: RecordFilter
    x: MetricId
    y: MetricId
    z: MetricId
}

export const configurationSerializer: Serializer<Configuration> = {
    toString: (configuration) => {
        const serializable: SerializableConfiguration = {
            mode: configuration.mode,
            filter: configuration.filter,
            x: configuration.x.id,
            y: configuration.y.id,
            z: configuration.z.id,
        }
        return JSON.stringify(serializable)
    },
    fromString: (s) => {
        const serializable: SerializableConfiguration = JSON.parse(s)
        return {
            mode: serializable.mode,
            filter: serializable.filter,
            x: getMetric(serializable.x),
            y: getMetric(serializable.y),
            z: getMetric(serializable.z),
        }
    },
}
