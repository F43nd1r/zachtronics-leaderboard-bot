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
import { VisualizerColor } from "../../../../utils/VisualizerColor"
import OmScore from "../../../../model/om/OmScore"
import { useState } from "react"
import { OmRecord } from "../../../../model/om/OmRecord"
import { RecordModal } from "../../../../components/RecordModal"
import OmRecordCard from "../../../../components/OmRecordCard"

export default function OmPuzzleVisualizerView() {
    const puzzleId = useParams().puzzleId
    const [activeRecords, setActiveRecords] = useState<OmRecord[] | undefined>(undefined)

    return (
        <>
            <Visualizer<OmScore, OmRecord>
                url={`/om/puzzle/${puzzleId}/records?includeFrontier=true`}
                config={{
                    key: "visualizerConfig",
                    default: {
                        mode: "3D",
                        x: { metric: "g", scale: "linear" },
                        y: { metric: "c", scale: "linear" },
                        z: { metric: "a", scale: "linear" },
                    },
                }}
                filter={{ key: `visualizerFilter-${puzzleId}`, default: { showOnlyFrontier: false } }}
                metrics={{
                    g: { name: "Cost", get: (score) => score?.cost },
                    i: { name: "Instructions", get: (score) => score?.instructions },
                    c: { name: "Cycles", get: (score) => score?.cycles },
                    a: { name: "Area", get: (score) => score?.area },
                    h: { name: "Height", get: (score) => score?.height },
                    w: { name: "Width", get: (score) => score?.width },
                    r: { name: "Rate", get: (score) => score?.rate },
                    aI: { name: "Area@∞", get: (score) => score?.areaINF },
                    hI: { name: "Height@∞", get: (score) => score?.heightINF },
                    wI: { name: "Width@∞", get: (score) => score?.widthINF },
                }}
                modifiers={{
                    overlap: {
                        get: (score) => score?.overlap,
                        name: "overlap",
                        color: VisualizerColor.MOD1,
                        legendOrder: -1,
                        option1: "Overlap",
                        option2: "Normal",
                    },
                    trackless: {
                        get: (score) => score?.trackless,
                        name: "trackless",
                        color: VisualizerColor.MOD2,
                        legendOrder: 1,
                        option1: "Trackless",
                        option2: "With Track",
                    },
                }}
                defaultColor={VisualizerColor.DEFAULT}
                onClick={setActiveRecords}
            />
            <RecordModal records={activeRecords} setRecords={setActiveRecords} RecordCardComponent={OmRecordCard} />
        </>
    )
}
