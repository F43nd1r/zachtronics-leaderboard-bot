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
import ScScore from "../../model/sc/ScScore"
import fetchFromApi from "../../utils/fetchFromApi"

export default function ScPuzzleVisualizerView() {
    const puzzleId = useParams().puzzleId
    fetchFromApi<Puzzle>(`/sc/puzzle/${puzzleId}`).then(
        (puzzle) => (document.title = `${puzzle.displayName} - SpaceChem Leaderboard`))

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
            <Visualizer<string, string, ScScore>
                url={`/sc/puzzle/${puzzleId}/records?includeFrontier=true`}
                config={{ key: "visualizerConfigSc", default: { mode: "3D", x: "c", y: "s", z: "r" } }}
                filter={{ key: `visualizerFilterSc-${puzzleId}`, default: {} }}
                metrics={{
                    c: { name: "Cycles", get: (score) => score?.cycles },
                    r: { name: "Reactors", get: (score) => score?.reactors },
                    s: { name: "Symbols", get: (score) => score?.symbols },
                }}
                modifiers={{
                    bugged: {
                        get: (score) => score?.bugged,
                        name: "bugged",
                        color: VisualizerColor.MOD1,
                        legendOrder: -1,
                        option1: "Bugged",
                        option2: "No Bugs",
                    },
                    precognitive: {
                        get: (score) => score?.precognitive,
                        name: "precognitive",
                        color: VisualizerColor.MOD2,
                        legendOrder: 1,
                        option1: "Precognitive",
                        option2: "No Precog",
                    },
                }}
                defaultColor={VisualizerColor.DEFAULT}
            />
        </Box>
    )
}
