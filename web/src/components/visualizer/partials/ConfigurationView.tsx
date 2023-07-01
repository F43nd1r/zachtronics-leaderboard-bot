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

import Metric from "../../../model/Metric"
import { Configuration, MetricConfiguration, Mode } from "../../../model/Configuration"
import { FormControl, MenuItem, Select, Stack, ToggleButton, ToggleButtonGroup } from "@mui/material"
import { MouseEvent, useEffect } from "react"
import FieldSet from "../../FieldSet"
import iterate from "../../../utils/iterate"

interface ConfigurationViewProps {
    metrics: Record<string, Metric<any>>
    configuration: Configuration
    setConfiguration: (configuration: Configuration) => void
}

export default function ConfigurationView(props: ConfigurationViewProps) {
    return (
        <FieldSet title="Configuration">
            <Stack spacing={1}>
                <ToggleButtonGroup
                    value={props.configuration.mode}
                    exclusive
                    onChange={(event: MouseEvent<HTMLElement>, newMode: Mode | null) => {
                        if (newMode !== null) {
                            props.setConfiguration({
                                ...props.configuration,
                                mode: newMode,
                            })
                        }
                    }}
                    aria-label="Mode"
                    fullWidth
                    size="small"
                >
                    <ToggleButton value={"2D"} aria-label={"2D"}>
                        2D
                    </ToggleButton>
                    <ToggleButton value={"3D"} aria-label={"3D"}>
                        3D
                    </ToggleButton>
                </ToggleButtonGroup>
                <MetricSelect
                    metrics={props.metrics}
                    value={props.configuration.x}
                    setValue={(value) =>
                        props.setConfiguration({
                            ...props.configuration,
                            x: value,
                        })
                    }
                />
                <MetricSelect
                    metrics={props.metrics}
                    value={props.configuration.y}
                    setValue={(value) =>
                        props.setConfiguration({
                            ...props.configuration,
                            y: value,
                        })
                    }
                />
                <MetricSelect
                    metrics={props.metrics}
                    value={props.configuration.z}
                    setValue={(value) =>
                        props.setConfiguration({
                            ...props.configuration,
                            z: value,
                        })
                    }
                    disabled={props.configuration.mode === "2D"}
                />
            </Stack>
        </FieldSet>
    )
}

interface MetricSelectProps {
    metrics: Record<string, Metric<any>>
    value: MetricConfiguration
    setValue: (metric: MetricConfiguration) => void
    disabled?: boolean
}

function MetricSelect(props: MetricSelectProps) {
    useEffect(() => {
        const validMetric = iterate(props.metrics).some(([metricId]) => metricId === props.value.metric)
        const validScale = props.value.scale === "linear" || props.value.scale === "log"
        if (!validMetric || !validScale) {
            props.setValue({
                metric: validMetric ? props.value.metric : Object.keys(props.metrics)[0],
                scale: validScale ? props.value.scale : "linear",
            })
        }
    })
    return (
        <div style={{ display: "flex" }}>
            <FormControl variant={"standard"} fullWidth>
                <Select
                    value={props.value.metric}
                    onChange={(event) => props.setValue({ ...props.value, metric: event.target.value as string })}
                    disabled={props.disabled}
                    fullWidth={true}
                >
                    {iterate(props.metrics).map(([metricId, metric]) => (
                        <MenuItem value={metricId} key={metricId}>
                            {metric.name}
                        </MenuItem>
                    ))}
                </Select>
            </FormControl>
            <FormControl variant={"standard"} fullWidth>
                <Select
                    value={props.value.scale}
                    onChange={(event) => props.setValue({ ...props.value, scale: event.target.value as "linear" | "log" })}
                    disabled={props.disabled}
                    fullWidth={true}
                >
                    <MenuItem value={"linear"}>Linear</MenuItem>
                    <MenuItem value={"log"}>Logarithmic</MenuItem>
                </Select>
            </FormControl>
        </div>
    )
}
