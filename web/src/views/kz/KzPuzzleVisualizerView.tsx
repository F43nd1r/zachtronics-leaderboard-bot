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

import { Box } from "@mui/material"
import { useParams } from "react-router-dom"
import { Visualizer } from "../../components/visualizer/Visualizer"
import { VisualizerColor } from "../../utils/VisualizerColor"
import Puzzle from "../../model/Puzzle"
import KzScore from "../../model/kz/KzScore"
import fetchFromApi from "../../utils/fetchFromApi"
import { useState } from "react"
import RecordDTO from "../../model/RecordDTO"
import { RecordModal } from "../../components/RecordModal"

export default function FcPuzzleVisualizerView() {
    const puzzleId = useParams().puzzleId
    fetchFromApi<Puzzle>(`/kz/puzzle/${puzzleId}`).then((puzzle) => (document.title = `${puzzle.displayName} - Kaizen Leaderboard`))
    const [activeRecords, setActiveRecords] = useState<RecordDTO<KzScore>[] | undefined>(undefined)

    return (
        <Box
            sx={{
                padding: 4,
                width: "100%",
                height: "100%",
                display: "flex",
                flexDirection: "column",
                minHeight: 0,
                flexGrow: 1,
                flexShrink: 1,
            }}
        >
            <Visualizer<KzScore>
                url={`/kz/puzzle/${puzzleId}/records?includeFrontier=true`}
                config={{
                    key: "visualizerConfigKz",
                    default: { mode: "3D", x: { metric: "t", scale: "linear" }, y: { metric: "c", scale: "linear" }, z: { metric: "a", scale: "linear" } },
                }}
                filter={{ key: `visualizerFilterFc-${puzzleId}`, default: {} }}
                metrics={{
                    t: { name: "Time", get: (score) => score?.time },
                    c: { name: "Cost", get: (score) => score?.cost },
                    a: { name: "Area", get: (score) => score?.area },
                }}
                modifiers={{}}
                defaultColor={VisualizerColor.DEFAULT}
                onClick={setActiveRecords}
            />
            <RecordModal records={activeRecords} setRecords={setActiveRecords} />
        </Box>
    )
}
