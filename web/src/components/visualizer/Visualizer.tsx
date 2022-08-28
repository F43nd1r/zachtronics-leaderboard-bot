/*
 * Copyright (c) 2022
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

import { Configuration } from "../../model/Configuration"
import { applyFilter, Filter } from "../../model/Filter"
import { Box, Stack } from "@mui/material"
import ApiResource from "../../utils/ApiResource"
import RecordDTO from "../../model/RecordDTO"
import ConfigurationView from "./partials/ConfigurationView"
import { FilterView } from "./partials/FilterView"
import PlotView from "./partials/PlotView"
import Metric from "../../model/Metric"
import Modifier from "../../model/Modifier"
import { LegendView } from "./partials/LegendView"
import { usePersistedUrlState } from "../../utils/usePersistedUrlState"

export interface VisualizerProps<MODIFIER_ID extends string, METRIC_ID extends string, SCORE, RECORD extends RecordDTO<SCORE>> {
    url: string
    config: {
        key: string
        default: Configuration<METRIC_ID>
    }
    filter: {
        key: string
        default: Filter<MODIFIER_ID, METRIC_ID>
    }
    metrics: Record<METRIC_ID, Metric<SCORE>>
    modifiers: Record<MODIFIER_ID, Modifier<SCORE>>
    defaultColor: string
    onClick: (records: RECORD[]) => void
}

export function Visualizer<MODIFIER_ID extends string, METRIC_ID extends string, SCORE, RECORD extends RecordDTO<SCORE> = RecordDTO<SCORE>>({
    config,
    defaultColor,
    filter: filterConfig,
    metrics,
    modifiers,
    onClick,
    url,
}: VisualizerProps<MODIFIER_ID, METRIC_ID, SCORE, RECORD>) {
    const [configuration, setConfiguration] = usePersistedUrlState(config.key, config.default)
    const [filter, setFilter] = usePersistedUrlState(filterConfig.key, filterConfig.default)

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
            <ApiResource<RECORD[]>
                url={url}
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
                            <ConfigurationView metrics={metrics} configuration={configuration} setConfiguration={setConfiguration} />
                            <FilterView
                                metrics={metrics}
                                modifiers={modifiers}
                                records={records}
                                filter={filter}
                                setFilter={(filter) => {
                                    if (applyFilter(metrics, modifiers, filter, configuration, records).length) {
                                        setFilter(filter)
                                        return true
                                    }
                                    return false
                                }}
                            />
                            <LegendView modifiers={modifiers} defaultColor={defaultColor} />
                        </Stack>
                        <PlotView metrics={metrics} modifiers={modifiers} defaultColor={defaultColor} records={records} configuration={configuration} filter={filter} onClick={onClick} />
                    </>
                )}
            />
        </Box>
    )
}
