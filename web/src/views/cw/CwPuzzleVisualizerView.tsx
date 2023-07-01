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
import CwScore from "../../model/cw/CwScore"
import fetchFromApi from "../../utils/fetchFromApi"
import { useState } from "react"
import RecordDTO from "../../model/RecordDTO"
import { RecordModal } from "../../components/RecordModal"

export default function CwPuzzleVisualizerView() {
    const puzzleId = useParams().puzzleId
    fetchFromApi<Puzzle>(`/cw/puzzle/${puzzleId}`).then((puzzle) => (document.title = `${puzzle.displayName} - ChipWizard™ Professional Leaderboard`))
    const [activeRecords, setActiveRecords] = useState<RecordDTO<CwScore>[] | undefined>(undefined)

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
            <Visualizer<CwScore>
                url={`/cw/puzzle/${puzzleId}/records?includeFrontier=true`}
                config={{
                    key: "visualizerConfigCw",
                    default: { mode: "3D", x: { metric: "w", scale: "linear" }, y: { metric: "h", scale: "linear" }, z: { metric: "f", scale: "linear" } },
                }}
                filter={{ key: `visualizerFilterCw-${puzzleId}`, default: {} }}
                metrics={{
                    w: { name: "Width", get: (score) => score?.width },
                    h: { name: "Height", get: (score) => score?.height },
                    s: { name: "Size", get: (score) => score && score?.width * score?.height },
                    f: { name: "Footprint", get: (score) => score?.footprint },
                }}
                modifiers={{}}
                defaultColor={VisualizerColor.DEFAULT}
                onClick={setActiveRecords}
            />
            <RecordModal records={activeRecords} setRecords={setActiveRecords} />
        </Box>
    )
}
