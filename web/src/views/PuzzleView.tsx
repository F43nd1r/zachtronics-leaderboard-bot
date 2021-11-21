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
import React, { useEffect, useState } from "react"
import { ComponentState } from "../utils/ComponentState"
import { Error } from "@mui/icons-material"
import Record from "../model/Record"
import RecordGrid from "../fragments/RecordGrid"
import LoadingIndicator from "../components/LoadingIndicator"
import fetchFromApi from "../utils/fetchFromApi"

export default function PuzzleView() {
    const params = useParams()
    const puzzleId = params.puzzleId as string

    const [state, setState] = useState(ComponentState.LOADING)
    const [records, setRecords] = useState<Record[]>([])
    useEffect(() => {
        fetchFromApi<Record[]>(`/puzzle/${puzzleId}/records`, setState).then((data) => setRecords(data))
    }, [puzzleId])

    let content: JSX.Element
    switch (state) {
        case ComponentState.LOADING:
            content = <LoadingIndicator />
            break
        case ComponentState.ERROR:
            content = (
                <>
                    <Error />
                    Failed to load records
                </>
            )
            break
        case ComponentState.READY:
            content = <RecordGrid records={records} getTitle={(record) => record.smartFormattedCategories ?? ""} />
            break
    }

    return content
}
