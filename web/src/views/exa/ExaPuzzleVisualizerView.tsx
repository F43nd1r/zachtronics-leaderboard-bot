/*
 * Copyright (c) 2024
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
import ExaScore from "../../model/exa/ExaScore"
import fetchFromApi from "../../utils/fetchFromApi"
import { RecordModal } from "../../components/RecordModal"
import { useState } from "react"
import RecordDTO from "../../model/RecordDTO"

export default function ExaPuzzleVisualizerView() {
    const puzzleId = useParams().puzzleId
    fetchFromApi<Puzzle>(`/exa/puzzle/${puzzleId}`).then((puzzle) => (document.title = `${puzzle.displayName} - Exapunks Leaderboard`))
    const [activeRecords, setActiveRecords] = useState<RecordDTO<ExaScore>[] | undefined>(undefined)

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
            <Visualizer<ExaScore>
                url={`/exa/puzzle/${puzzleId}/records?includeFrontier=true`}
                config={{
                    key: "visualizerConfigExa",
                    default: { mode: "3D", x: { metric: "c", scale: "linear" }, y: { metric: "s", scale: "linear" }, z: { metric: "a", scale: "linear" } },
                }}
                filter={{ key: `visualizerFilterExa-${puzzleId}`, default: {} }}
                metrics={{
                    c: { name: "Cycles", get: (score) => score?.cycles },
                    s: { name: "Size", get: (score) => score?.size },
                    a: { name: "Activity", get: (score) => score?.activity },
                }}
                modifiers={{
                    cheesy: {
                        get: (score) => score?.cheesy,
                        name: "cheesy",
                        color: VisualizerColor.BAD2,
                        legendOrder: -1,
                        option1: "Cheesy",
                        option2: "Legit",
                    },
                }}
                defaultColor={VisualizerColor.DEFAULT}
                onClick={setActiveRecords}
            />
            <RecordModal records={activeRecords} setRecords={setActiveRecords} />
        </Box>
    )
}
