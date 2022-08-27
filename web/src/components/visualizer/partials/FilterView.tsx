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

import RecordDTO from "../../../model/RecordDTO"
import { SxProps } from "@mui/system"
import { Theme } from "@mui/material/styles"
import { Box, Slider, Stack, ToggleButton, ToggleButtonGroup, Typography } from "@mui/material"
import { Filter } from "../../../model/Filter"
import FieldSet from "../../FieldSet"
import Metric from "../../../model/Metric"
import Modifier from "../../../model/Modifier"
import iterate from "../../../utils/iterate"
import { ReactNode } from "react"
import * as d3 from "d3-format"

interface FilterProps<MODIFIER_ID extends string, METRIC_ID extends string, SCORE> {
    metrics: Record<METRIC_ID, Metric<SCORE>>
    modifiers: Record<MODIFIER_ID, Modifier<SCORE>>
    filter: Filter<MODIFIER_ID, METRIC_ID>
    setFilter: (filter: Filter<MODIFIER_ID, METRIC_ID>) => boolean
    records: RecordDTO<SCORE>[]
}

export function FilterView<MODIFIER_ID extends string, METRIC_ID extends string, SCORE>(props: FilterProps<MODIFIER_ID, METRIC_ID, SCORE>) {
    return (
        <FieldSet title={"Filter"}>
            <Stack spacing={1}>
                <ToggleButton
                    value={"check"}
                    selected={props.filter.showOnlyFrontier ?? false}
                    onChange={() => {
                        props.setFilter({
                            ...props.filter,
                            showOnlyFrontier: !props.filter.showOnlyFrontier,
                        })
                    }}
                    sx={{ textTransform: "none" }}
                >
                    Show only frontier
                </ToggleButton>
                {iterate(props.modifiers).map(([modifierId, modifier]) => (
                    <FilterButtonGroup
                        key={modifierId}
                        filter={props.filter.modifiers && props.filter.modifiers[modifierId]}
                        setFilter={(value) =>
                            props.setFilter({
                                ...props.filter,
                                modifiers: {
                                    ...(props.filter.modifiers ?? ({} as Record<MODIFIER_ID, Modifier<SCORE>>)),
                                    [modifierId]: value,
                                },
                            })
                        }
                        label={modifier.name}
                        option1={modifier.option1}
                        option2={modifier.option2}
                    />
                ))}
                {iterate(props.metrics).map(([metricId, metric]) => (
                    <FilterSlider
                        key={metricId}
                        value={props.filter.max?.[metricId]}
                        setValue={(value) =>
                            props.setFilter({
                                ...props.filter,
                                max: {
                                    ...(props.filter.max ?? ({} as Record<METRIC_ID, Metric<SCORE>>)),
                                    [metricId]: value,
                                },
                            })
                        }
                        values={props.records.map((record) => metric.get(record.score))}
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
    option1: ReactNode
    option2: ReactNode
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
            <ToggleButton value={"on"} aria-label={`${props.label}-on`} sx={{ textTransform: "none" }}>
                {props.option1}
            </ToggleButton>
            <ToggleButton value={"off"} aria-label={`${props.label}-off`} sx={{ textTransform: "none" }}>
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

const numberFormat = d3.format(".3~f")

function FilterSlider(props: FilterSliderProps) {
    const values = [...new Set(props.values)].map((value) => (value !== undefined ? value : Infinity)).sort((a, b) => a - b)
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
                valueLabelFormat={(index) => (values[index] !== undefined ? numberFormat(values[index]) : "∞")}
                valueLabelDisplay={"on"}
                value={props.value !== undefined ? values.indexOf(props.value) : values.length - 1}
                onChange={(event, i) => {
                    const index = i as number
                    const value = values[index]
                    if (value !== undefined) {
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
