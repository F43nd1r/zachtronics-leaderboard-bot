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

import { useParams } from "react-router-dom"
import React from "react"
import Record from "../../../model/Record"
import RecordGrid from "../../../fragments/RecordGrid"
import ApiResource from "../../../utils/ApiResource"

export default function PuzzleRecordsView() {
    return (
        <ApiResource<Record[]>
            url={`/puzzle/${useParams().puzzleId}/records`}
            element={(records) => (
                <RecordGrid
                    records={records}
                    getTitle={(record) => record.smartFormattedCategories || "Pareto Frontier"}
                    getScore={(record) => record.smartFormattedScore ?? record.fullFormattedScore ?? "None"}
                />
            )}
        />
    )
}
