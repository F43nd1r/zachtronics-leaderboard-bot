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
import SzScore from "../../model/sz/SzScore"
import fetchFromApi from "../../utils/fetchFromApi"
import { RecordModal } from "../../components/RecordModal"
import { useState } from "react"
import RecordDTO from "../../model/RecordDTO"

export default function SzPuzzleVisualizerView() {
    const puzzleId = useParams().puzzleId
    fetchFromApi<Puzzle>(`/sz/puzzle/${puzzleId}`).then((puzzle) => (document.title = `${puzzle.displayName} - Shenzhen I/O Leaderboard`))
    const [activeRecords, setActiveRecords] = useState<RecordDTO<SzScore>[] | undefined>(undefined)

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
            <Visualizer<string, string, SzScore>
                url={`/sz/puzzle/${puzzleId}/records?includeFrontier=true`}
                config={{ key: "visualizerConfigSz", default: { mode: "3D", x: "p", y: "c", z: "l" } }}
                filter={{ key: `visualizerFilterSz-${puzzleId}`, default: {} }}
                metrics={{
                    c: { name: "Cost", get: (score) => score?.cost },
                    p: { name: "Power", get: (score) => score?.power },
                    l: { name: "Lines", get: (score) => score?.lines },
                }}
                modifiers={{}}
                defaultColor={VisualizerColor.DEFAULT}
                onClick={setActiveRecords}
            />
            <RecordModal records={activeRecords} setRecords={setActiveRecords} />
        </Box>
    )
}
