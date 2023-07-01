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
import FpScore from "../../model/fp/FpScore"
import fetchFromApi from "../../utils/fetchFromApi"
import { useState } from "react"
import RecordDTO from "../../model/RecordDTO"
import { RecordModal } from "../../components/RecordModal"

export default function FpPuzzleVisualizerView() {
    const puzzleId = useParams().puzzleId
    fetchFromApi<Puzzle>(`/fp/puzzle/${puzzleId}`).then((puzzle) => (document.title = `${puzzle.displayName} - X'BPGH: The Forbidden Path Leaderboard`))
    const [activeRecords, setActiveRecords] = useState<RecordDTO<FpScore>[] | undefined>(undefined)

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
            <Visualizer<string, string, FpScore>
                url={`/fp/puzzle/${puzzleId}/records?includeFrontier=true`}
                config={{
                    key: "visualizerConfigFp",
                    default: { mode: "3D", x: { metric: "f", scale: "linear" }, y: { metric: "r", scale: "linear" }, z: { metric: "c", scale: "linear" } },
                }}
                filter={{ key: `visualizerFilterFp-${puzzleId}`, default: {} }}
                metrics={{
                    r: { name: "Rules", get: (score) => score?.rules },
                    c: { name: "Conditional Rules", get: (score) => score?.conditionalRules },
                    f: { name: "Frames", get: (score) => score?.frames },
                    w: { name: "Waste", get: (score) => score?.waste },
                }}
                modifiers={{}}
                defaultColor={VisualizerColor.DEFAULT}
                onClick={setActiveRecords}
            />
            <RecordModal records={activeRecords} setRecords={setActiveRecords} />
        </Box>
    )
}
