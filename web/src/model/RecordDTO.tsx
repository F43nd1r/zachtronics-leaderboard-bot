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

export default interface RecordDTO<SCORE> {
    score?: SCORE
    fullFormattedScore?: string
    gif?: string
    solution?: string
    smartFormattedCategories?: string
}

export function isStrictlyBetterInMetrics<SCORE>(r1: RecordDTO<SCORE>, r2: RecordDTO<SCORE>, metrics: Metric<SCORE>[]): boolean {
    const compares = metrics.map((metric) => (metric.get(r1.score) ?? Number.MAX_SAFE_INTEGER) - (metric.get(r2.score) ?? Number.MAX_SAFE_INTEGER))
    return !compares.some((compare) => compare > 0) && compares.some((compare) => compare < 0)
}
