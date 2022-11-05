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
import IfScore from "../../model/if/IfScore"
import fetchFromApi from "../../utils/fetchFromApi"
import { RecordModal } from "../../components/RecordModal"
import { useState } from "react"
import RecordDTO from "../../model/RecordDTO"

export default function IfPuzzleVisualizerView() {
    const puzzleId = useParams().puzzleId
    fetchFromApi<Puzzle>(`/if/puzzle/${puzzleId}`).then((puzzle) => (document.title = `${puzzle.displayName} - Infinifactory Leaderboard`))
    const [activeRecords, setActiveRecords] = useState<RecordDTO<IfScore>[] | undefined>(undefined)

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
            <Visualizer<string, string, IfScore>
                url={`/if/puzzle/${puzzleId}/records?includeFrontier=true`}
                config={{ key: "visualizerConfigIf", default: { mode: "3D", x: "c", y: "f", z: "b" } }}
                filter={{ key: `visualizerFilterIf-${puzzleId}`, default: {} }}
                metrics={{
                    c: { name: "Cycles", get: (score) => score?.cycles },
                    f: { name: "Footprint", get: (score) => score?.footprint },
                    b: { name: "Blocks", get: (score) => score?.blocks },
                }}
                modifiers={{
                    usesGRA: {
                        get: (score) => score?.usesGRA,
                        name: "usesGRA",
                        color: VisualizerColor.MOD1,
                        legendOrder: -1,
                        option1: "Uses GRA",
                        option2: "No GRA",
                    },
                    finite: {
                        get: (score) => score?.finite,
                        name: "finite",
                        color: VisualizerColor.MOD2,
                        legendOrder: 1,
                        option1: "Finite",
                        option2: "Infinite",
                    },
                }}
                defaultColor={VisualizerColor.DEFAULT}
                onClick={setActiveRecords}
            />
            <RecordModal records={activeRecords} setRecords={setActiveRecords} />
        </Box>
    )
}
