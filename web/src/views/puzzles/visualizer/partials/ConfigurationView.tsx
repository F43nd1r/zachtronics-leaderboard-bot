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

import { MetricId, metrics } from "../../../../model/Metric"
import { Configuration, Mode } from "../../../../model/Configuration"
import { FormControl, MenuItem, Select, Stack, ToggleButton, ToggleButtonGroup } from "@mui/material"
import { MouseEvent } from "react"
import FieldSet from "../../../../components/FieldSet"

interface ConfigurationViewProps {
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
            </Stack>
        </FieldSet>
    )
}

interface MetricSelectProps {
    value: MetricId
    setValue: (metric: MetricId) => void
    disabled?: boolean
}

function MetricSelect(props: MetricSelectProps) {
    return (
        <FormControl variant={"standard"} fullWidth>
            <Select value={props.value} onChange={(event) => props.setValue(event.target.value as MetricId)} disabled={props.disabled} fullWidth={true}>
                {metrics.map((metric) => (
                    <MenuItem value={metric.id} key={metric.id}>
                        {metric.name}
                    </MenuItem>
                ))}
            </Select>
        </FormControl>
    )
}
