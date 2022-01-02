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
import { Box, Stack } from "@mui/material"
import OmRecord from "../../../model/Record"
import { usePersistedJsonState } from "../../../utils/usePersistedState"
import ApiResource from "../../../utils/ApiResource"
import PlotView from "./partials/PlotView"
import { Configuration } from "../../../model/Configuration"
import ConfigurationView from "./partials/ConfigurationView"
import { applyFilter, Filter } from "../../../model/Filter"
import { FilterView } from "./partials/FilterView"
import { LegendView } from "./partials/LegendView"

export default function PuzzleVisualizerView() {
    const puzzleId = useParams().puzzleId
    const [configuration, setConfiguration] = usePersistedJsonState<Configuration>("visualizerConfig", {
        mode: "3D",
        x: "g",
        y: "c",
        z: "a",
    })
    const [filter, setFilter] = usePersistedJsonState<Filter>(`visualizerFilter-${puzzleId}`, {})

    return (
        <Box
            sx={(theme) => ({
                display: "flex",
                minHeight: 0,
                flexGrow: 1,
                flexShrink: 1,
                [theme.breakpoints.down("md")]: {
                    flexDirection: "column",
                    minHeight: "unset",
                },
            })}
        >
            <ApiResource<OmRecord[]>
                url={`/puzzle/${puzzleId}/records?includeFrontier=true`}
                element={(records) => (
                    <>
                        <Stack
                            spacing={1}
                            sx={(theme) => ({
                                width: "100%",
                                [theme.breakpoints.up("md")]: {
                                    width: "30rem",
                                    height: "100%",
                                    overflowY: "auto",
                                },
                            })}
                        >
                            <ConfigurationView configuration={configuration} setConfiguration={setConfiguration} />
                            <FilterView
                                records={records}
                                filter={filter}
                                setFilter={(filter) => {
                                    if (applyFilter(filter, configuration, records).length) {
                                        setFilter(filter)
                                        return true
                                    }
                                    return false
                                }}
                            />
                            <LegendView />
                        </Stack>
                        <PlotView records={records} configuration={configuration} filter={filter} />
                    </>
                )}
            />
        </Box>
    )
}
