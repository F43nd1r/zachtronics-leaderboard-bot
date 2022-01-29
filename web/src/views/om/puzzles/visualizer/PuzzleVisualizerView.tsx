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
import { Visualizer } from "../../../../components/visualizer/Visualizer"
import OmScore from "../../../../model/om/OmScore"

export default function PuzzleVisualizerView() {
    const puzzleId = useParams().puzzleId

    return (
        <Visualizer<string, string, OmScore>
            url={`/om/puzzle/${puzzleId}/records?includeFrontier=true`}
            config={{ key: "visualizerConfig", default: { mode: "3D", x: "g", y: "c", z: "a" } }}
            filter={{ key: `visualizerFilter-${puzzleId}`, default: { showOnlyFrontier: false } }}
            metrics={{
                g: { name: "Cost", get: (score) => score?.cost },
                c: { name: "Cycles", get: (score) => score?.cycles },
                a: { name: "Area", get: (score) => score?.area },
                i: { name: "Instructions", get: (score) => score?.instructions },
                h: { name: "Height", get: (score) => score?.height },
                w: { name: "Width", get: (score) => score?.width },
                r: { name: "Rate", get: (score) => score?.rate },
            }}
            modifiers={{
                overlap: {
                    get: (score) => score?.overlap,
                    name: "overlap",
                    color: "#880e4f",
                    legendOrder: -1,
                    option1: "Overlap",
                    option2: "Normal",
                },
                trackless: {
                    get: (score) => score?.trackless,
                    name: "trackless",
                    color: "#558b2f",
                    legendOrder: 1,
                    option1: "Trackless",
                    option2: "With Track",
                },
            }}
            defaultColor={"#0288d1"}
        />
    )
}
