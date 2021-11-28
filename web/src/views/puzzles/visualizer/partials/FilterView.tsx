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

import OmRecord from "../../../../model/Record"
import { metrics } from "../../../../model/Metric"
import { SxProps } from "@mui/system"
import { Theme } from "@mui/material/styles"
import { Box, Slider, Stack, ToggleButton, ToggleButtonGroup, Typography } from "@mui/material"
import { Filter } from "../../../../model/Filter"
import FieldSet from "../../../../components/FieldSet"

interface FilterProps {
    filter: Filter
    setFilter: (filter: Filter) => boolean
    records: OmRecord[]
}

export function FilterView(props: FilterProps) {
    return (
        <FieldSet title={"Filter"}>
            <Stack spacing={1}>
                <FilterButtonGroup
                    filter={props.filter.overlap}
                    setFilter={(value) =>
                        props.setFilter({
                            ...props.filter,
                            overlap: value,
                        })
                    }
                    label={"Overlap"}
                    option1={"Overlap"}
                    option2={"Normal"}
                />
                <FilterButtonGroup
                    filter={props.filter.trackless}
                    setFilter={(value) =>
                        props.setFilter({
                            ...props.filter,
                            trackless: value,
                        })
                    }
                    label={"Trackless"}
                    option1={"Trackless"}
                    option2={"With Track"}
                    sx={{ paddingBottom: 1 }}
                />
                {metrics.map((metric) => (
                    <FilterSlider
                        key={metric.id}
                        value={props.filter.max?.[metric.id]}
                        setValue={(value) =>
                            props.setFilter({
                                ...props.filter,
                                max: {
                                    ...(props.filter.max ?? {
                                        g: undefined,
                                        c: undefined,
                                        a: undefined,
                                        i: undefined,
                                        h: undefined,
                                        w: undefined,
                                        r: undefined,
                                    }),
                                    [metric.id]: value,
                                },
                            })
                        }
                        values={props.records.map((record) => metric.get(record))}
                        label={metric.name}
                    />
                ))}
            </Stack>
        </FieldSet>
    )
}

interface FilterButtonGroupProps {
    filter?: boolean
    setFilter: (filter: boolean | undefined) => void
    label: string
    option1: string
    option2: string
    sx?: SxProps<Theme>
}

function FilterButtonGroup(props: FilterButtonGroupProps) {
    return (
        <ToggleButtonGroup
            value={props.filter !== undefined ? [props.filter ? "on" : "off"] : ["on", "off"]}
            onChange={(_, newFilterValue: string[]) => {
                if (newFilterValue.length) {
                    props.setFilter(newFilterValue.length === 1 ? newFilterValue[0] === "on" : undefined)
                } else if (props.filter !== undefined) {
                    props.setFilter(!props.filter)
                }
            }}
            aria-label={props.label}
            sx={props.sx}
            fullWidth
            size="small"
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

interface FilterSliderProps {
    value?: number
    setValue: (filter: number | undefined) => boolean
    values: (number | undefined)[]
    label: string
}

function FilterSlider(props: FilterSliderProps) {
    const values = [...new Set(props.values)].map((value) => value ?? Infinity).sort((a, b) => a - b)
    return (
        <Box
            sx={{
                width: "100%",
                paddingLeft: "1.5rem",
                paddingRight: "1.5rem",
            }}
        >
            <Typography id={`filter-slider-${props.label}`}>{props.label}</Typography>
            <Slider
                aria-labelledby={`filter-slider-${props.label}`}
                valueLabelFormat={(index) => values[index]?.toString() ?? "∞"}
                valueLabelDisplay={"on"}
                value={props.value ? values.indexOf(props.value) : values.length - 1}
                onChange={(event, i) => {
                    const index = i as number
                    const value = values[index]
                    if (value) {
                        if (!props.setValue(value !== Infinity && value !== values[values.length - 1] ? value : undefined)) {
                            event.preventDefault()
                        }
                    }
                }}
                step={1}
                min={0}
                max={values.length - 1}
                sx={{
                    width: "100%",
                }}
            />
        </Box>
    )
}
