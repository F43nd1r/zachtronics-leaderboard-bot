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

import { getMetric, Metric, MetricId, metrics } from "../../../model/Metric"
import { Configuration, Mode } from "./Configuration"
import { Box, MenuItem, Select, ToggleButton, ToggleButtonGroup } from "@mui/material"
import { Legend } from "./Legend"

interface PuzzleFrontierConfigurationProps {
    configuration: Configuration
    setConfiguration: (configuration: Configuration) => void
}

export default function PuzzleFrontierConfiguration(props: PuzzleFrontierConfigurationProps) {
    const handleMode = (event: React.MouseEvent<HTMLElement>, newMode: Mode | null) => {
        if (newMode !== null) {
            props.setConfiguration({
                ...props.configuration,
                mode: newMode,
            })
        }
    }
    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: "column",
            }}
        >
            <ToggleButtonGroup value={props.configuration.mode} exclusive onChange={handleMode} aria-label="Mode" fullWidth={true}>
                <ToggleButton value={"2D"} aria-label={"2D"}>
                    2D
                </ToggleButton>
                <ToggleButton value={"3D"} aria-label={"3D"}>
                    3D
                </ToggleButton>
            </ToggleButtonGroup>
            <MetricSelect
                value={props.configuration.x}
                setValue={(value) =>
                    props.setConfiguration({
                        ...props.configuration,
                        x: value,
                    })
                }
            />
            <MetricSelect
                value={props.configuration.y}
                setValue={(value) =>
                    props.setConfiguration({
                        ...props.configuration,
                        y: value,
                    })
                }
            />
            <MetricSelect
                value={props.configuration.z}
                setValue={(value) =>
                    props.setConfiguration({
                        ...props.configuration,
                        z: value,
                    })
                }
                disabled={props.configuration.mode === "2D"}
            />
            <FilterButtonGroup
                filter={props.configuration.filter.overlap}
                setFilter={(value) =>
                    props.setConfiguration({
                        ...props.configuration,
                        filter: {
                            ...props.configuration.filter,
                            overlap: value,
                        },
                    })
                }
                label={"Overlap"}
                option1={"Overlap"}
                option2={"Normal"}
            />
            <FilterButtonGroup
                filter={props.configuration.filter.trackless}
                setFilter={(value) =>
                    props.setConfiguration({
                        ...props.configuration,
                        filter: {
                            ...props.configuration.filter,
                            trackless: value,
                        },
                    })
                }
                label={"Trackless"}
                option1={"Trackless"}
                option2={"With Track"}
            />
            <Legend />
        </Box>
    )
}

interface FilterButtonGroupProps {
    filter?: boolean
    setFilter: (filter: boolean | undefined) => void
    label: string
    option1: string
    option2: string
}

function FilterButtonGroup(props: FilterButtonGroupProps) {
    const handleChange = (event: React.MouseEvent<HTMLElement>, newFilterValue: string[]) => {
        if (newFilterValue.length) {
            props.setFilter(newFilterValue.length === 1 ? newFilterValue[0] === "on" : undefined)
        } else if (props.filter !== undefined) {
            props.setFilter(!props.filter)
        }
    }
    return (
        <ToggleButtonGroup
            value={props.filter !== undefined ? [props.filter ? "on" : "off"] : ["on", "off"]}
            onChange={handleChange}
            aria-label={props.label}
            sx={{ marginTop: "1rem" }}
            fullWidth={true}
        >
            <ToggleButton value={"on"} aria-label={`${props.label}-on`}>
                {props.option1}
            </ToggleButton>
            <ToggleButton value={"off"} aria-label={`${props.label}-off`}>
                {props.option2}
            </ToggleButton>
        </ToggleButtonGroup>
    )
}

interface MetricSelectProps {
    value: Metric
    setValue: (metric: Metric) => void
    disabled?: boolean
}

function MetricSelect(props: MetricSelectProps) {
    return (
        <Select value={props.value.id} onChange={(event) => props.setValue(getMetric(event.target.value as MetricId))} sx={{ marginTop: "1rem" }} disabled={props.disabled} fullWidth={true}>
            {metrics.map((metric) => (
                <MenuItem value={metric.id} key={metric.id}>
                    {metric.name}
                </MenuItem>
            ))}
        </Select>
    )
}
