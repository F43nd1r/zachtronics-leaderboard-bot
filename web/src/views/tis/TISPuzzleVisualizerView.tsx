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
import TISScore from "../../model/tis/TISScore"
import fetchFromApi from "../../utils/fetchFromApi"
import { RecordModal } from "../../components/RecordModal"
import { useState } from "react"
import RecordDTO from "../../model/RecordDTO"

export default function TISPuzzleVisualizerView() {
    const puzzleId = useParams().puzzleId
    fetchFromApi<Puzzle>(`/tis/puzzle/${puzzleId}`).then((puzzle) => (document.title = `${puzzle.displayName} - TIS-100 Leaderboard`))
    const [activeRecords, setActiveRecords] = useState<RecordDTO<TISScore>[] | undefined>(undefined)

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
            <Visualizer<TISScore>
                url={`/tis/puzzle/${puzzleId}/records?includeFrontier=true`}
                config={{
                    key: "visualizerConfigTIS",
                    default: { mode: "3D", x: { metric: "c", scale: "linear" }, y: { metric: "n", scale: "linear" }, z: { metric: "i", scale: "linear" } },
                }}
                filter={{ key: `visualizerFilterTIS-${puzzleId}`, default: {} }}
                metrics={{
                    c: { name: "Cycles", get: (score) => score?.cycles },
                    n: { name: "Nodes", get: (score) => score?.nodes },
                    i: { name: "Instructions", get: (score) => score?.instructions },
                }}
                modifiers={{
                    cheating: {
                        get: (score) => score?.cheating,
                        name: "cheating",
                        color: VisualizerColor.BAD1,
                        legendOrder: -1,
                        option1: "Cheating",
                        option2: "Legit",
                    },
                    achievement: {
                        get: (score) => score?.achievement,
                        name: "achievement",
                        color: VisualizerColor.GOOD,
                        legendOrder: 1,
                        option1: "Achievement",
                        option2: "No Achievement",
                    },
                }}
                defaultColor={VisualizerColor.DEFAULT}
                onClick={setActiveRecords}
            />
            <RecordModal records={activeRecords} setRecords={setActiveRecords} />
        </Box>
    )
}
