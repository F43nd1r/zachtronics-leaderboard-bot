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
import { Box } from "@mui/material"
import OmRecord from "../../../model/Record"
import { usePersistedState } from "../../../utils/usePersistedState"
import ApiResource from "../../../utils/ApiResource"
import PuzzleFrontierPlot from "./PuzzleFrontierPlot"
import { getMetric } from "../../../model/Metric"
import { configurationSerializer } from "./Configuration"
import PuzzleFrontierConfiguration from "./PuzzleFrontierConfiguration"

export default function PuzzleVisualizerView() {
    const [configuration, setConfiguration] = usePersistedState(
        "visualizerConfig",
        {
            mode: "3D",
            filter: {},
            x: getMetric("g"),
            y: getMetric("c"),
            z: getMetric("a"),
        },
        configurationSerializer
    )

    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: "row",
                height: "100%",
            }}
        >
            <PuzzleFrontierConfiguration configuration={configuration} setConfiguration={setConfiguration} />
            <ApiResource<OmRecord[]>
                url={`/puzzle/${useParams().puzzleId}/records?includeFrontier=true`}
                element={(records) => <PuzzleFrontierPlot records={records} configuration={configuration} />}
            />
        </Box>
    )
}
