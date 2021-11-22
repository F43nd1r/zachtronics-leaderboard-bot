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

import Plot from "react-plotly.js"
import { withSize } from "react-sizeme"
import { useParams } from "react-router-dom"
import React, { useEffect, useState } from "react"
import { ComponentState } from "../utils/ComponentState"
import fetchFromApi from "../utils/fetchFromApi"
import LoadingIndicator from "../components/LoadingIndicator"
import { Error } from "@mui/icons-material"
import { Box, MenuItem, Modal, Select, ToggleButton, ToggleButtonGroup, useTheme } from "@mui/material"
import OmRecord from "../model/Record"
import RecordCard from "../fragments/RecordCard"

interface PuzzleFrontierPlotProps {
    size: { width: number; height: number }
    records: OmRecord[]
    mode: Mode
    x: Metric
    y: Metric
    z: Metric
}

function PuzzleFrontierPlot(props: PuzzleFrontierPlotProps) {
    const theme = useTheme()

    const [activeRecord, setActiveRecord] = useState<OmRecord | undefined>(undefined)

    return (
        <>
            <Plot
                data={[
                    {
                        x: props.records.map((record) => props.x.get(record) ?? 0),
                        y: props.records.map((record) => props.y.get(record) ?? 0),
                        z: props.records.map((record) => props.z.get(record) ?? 0),
                        hovertext: props.records.map((record) => record.fullFormattedScore ?? "none"),
                        hovertemplate:
                            (props.mode === "2D" && `${props.x.name}: %{x}<br>${props.y.name}: %{y}<br>%{hovertext}<extra></extra>`) ||
                            `${props.x.name}: %{x}<br>${props.y.name}: %{y}<br>${props.z.name}: %{z}<br>%{hovertext}<extra></extra>`,
                        type: (props.mode === "2D" && "scatter") || "scatter3d",
                        mode: "markers",
                        marker: {
                            size: (props.mode === "2D" && 20) || 10,
                        },
                    },
                ]}
                layout={{
                    autosize: false,
                    width: props.size.width ?? undefined,
                    height: props.size.height ?? undefined,
                    paper_bgcolor: "transparent",
                    plot_bgcolor: "transparent",
                    xaxis: {
                        title: props.x.name,
                        color: theme.palette.text.primary,
                        nticks: (props.x.isBoolean && 2) || undefined,
                    },
                    yaxis: {
                        title: props.y.name,
                        color: theme.palette.text.primary,
                        nticks: (props.y.isBoolean && 2) || undefined,
                    },
                    legend: {
                        font: {
                            color: theme.palette.text.primary,
                        },
                    },
                    title: {
                        font: {
                            color: theme.palette.text.primary,
                        },
                    },
                    hoverlabel: {
                        bgcolor: theme.palette.background.paper,
                    },
                    scene: {
                        xaxis: {
                            title: props.x.name,
                            color: theme.palette.text.primary,
                            nticks: (props.x.isBoolean && 2) || undefined,
                        },
                        yaxis: {
                            title: props.y.name,
                            color: theme.palette.text.primary,
                            nticks: (props.y.isBoolean && 2) || undefined,
                        },
                        zaxis: {
                            title: props.z.name,
                            color: theme.palette.text.primary,
                            nticks: (props.z.isBoolean && 2) || undefined,
                        },
                    },
                }}
                config={{
                    displaylogo: false,
                }}
                style={{
                    width: "100%",
                    height: "100%",
                    flexGrow: 1,
                }}
                useResizeHandler={true}
                onClick={(event) => {
                    const point: any = event.points[0]
                    const record = props.records.find((record) => props.x.get(record) === point.x && props.y.get(record) === point.y && (props.mode === "2D" || props.z.get(record) === point.z))
                    setActiveRecord(record)
                }}
            />
            <RecordModal record={activeRecord} setRecord={setActiveRecord} />
        </>
    )
}

interface RecordModalProps {
    record?: OmRecord
    setRecord: (record: OmRecord | undefined) => void
}

function RecordModal(props: RecordModalProps) {
    return (
        <Modal open={props.record !== undefined} onClose={() => props.setRecord(undefined)}>
            <Box
                sx={{
                    position: "absolute",
                    top: "50%",
                    left: "50%",
                    transform: "translate(-50%, -50%)",
                    boxShadow: 24,
                    outline: 0,
                }}
            >
                <RecordCard
                    record={props.record!}
                    title={props.record?.smartFormattedCategories || "Pareto Frontier"}
                    sx={{
                        paddingLeft: "1rem",
                        paddingRight: "1rem",
                    }}
                />
            </Box>
        </Modal>
    )
}

const SizedPuzzleFrontierPlot = withSize({ monitorHeight: true })<PuzzleFrontierPlotProps>(PuzzleFrontierPlot)

type Mode = "2D" | "3D"

type MetricId = "g" | "c" | "a" | "i" | "h" | "w" | "r" | "t" | "o"

interface Metric {
    id: MetricId
    name: string
    get: (record: OmRecord) => number | undefined
    isBoolean?: boolean
}

const metrics: Metric[] = [
    {
        id: "g",
        name: "Cost",
        get(record) {
            return record.score?.cost
        },
    },
    {
        id: "c",
        name: "Cycles",
        get(record) {
            return record.score?.cycles
        },
    },
    {
        id: "a",
        name: "Area",
        get(record) {
            return record.score?.area
        },
    },
    {
        id: "i",
        name: "Instructions",
        get(record) {
            return record.score?.instructions
        },
    },
    {
        id: "h",
        name: "Height",
        get(record) {
            return record.score?.height
        },
    },
    {
        id: "w",
        name: "Width",
        get(record) {
            return record.score?.width
        },
    },
    {
        id: "r",
        name: "Rate",
        get(record) {
            return record.score?.rate
        },
    },
    {
        id: "t",
        name: "Trackless",
        isBoolean: true,
        get(record) {
            return (record.score?.trackless && 1) || 0
        },
    },
    {
        id: "o",
        name: "Overlap",
        isBoolean: true,
        get(record) {
            return (record.score?.overlap && 1) || 0
        },
    },
]

function getMetric(id: MetricId) {
    return metrics.find((metric) => metric.id === id)!
}

interface PuzzleFrontierConfigurationProps {
    mode: Mode
    setMode: (mode: Mode) => void
    x: Metric
    setX: (metric: Metric) => void
    y: Metric
    setY: (metric: Metric) => void
    z: Metric
    setZ: (metric: Metric) => void
}

function PuzzleFrontierConfiguration(props: PuzzleFrontierConfigurationProps) {
    const handleMode = (event: React.MouseEvent<HTMLElement>, newMode: Mode | null) => {
        if (newMode !== null) {
            props.setMode(newMode)
        }
    }
    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: "column",
            }}
        >
            <ToggleButtonGroup value={props.mode} exclusive onChange={handleMode} aria-label="Mode">
                <ToggleButton value={"2D"} aria-label={"2D"}>
                    2D
                </ToggleButton>
                <ToggleButton value={"3D"} aria-label={"3D"}>
                    3D
                </ToggleButton>
            </ToggleButtonGroup>
            <MetricSelect value={props.x} setValue={props.setX} />
            <MetricSelect value={props.y} setValue={props.setY} />
            <MetricSelect value={props.z} setValue={props.setZ} disabled={props.mode === "2D"} />
        </Box>
    )
}

interface MetricSelectProps {
    value: Metric
    setValue: (metric: Metric) => void
    disabled?: boolean
}

function MetricSelect(props: MetricSelectProps) {
    return (
        <Select value={props.value.id} onChange={(event) => props.setValue(getMetric(event.target.value as MetricId))} sx={{ marginTop: "1rem" }} disabled={props.disabled}>
            {metrics.map((metric) => (
                <MenuItem value={metric.id} key={metric.id}>
                    {metric.name}
                </MenuItem>
            ))}
        </Select>
    )
}

export default function PuzzleFrontierView() {
    const params = useParams()
    const puzzleId = params.puzzleId as string

    const [state, setState] = useState(ComponentState.LOADING)
    const [records, setRecords] = useState<OmRecord[]>([])
    useEffect(() => {
        fetchFromApi<OmRecord[]>(`/puzzle/${puzzleId}/records?includeFrontier=true`, setState).then((data) => setRecords(data))
    }, [puzzleId])

    const [mode, setMode] = useState<Mode>("3D")
    const [x, setX] = useState<Metric>(getMetric("g"))
    const [y, setY] = useState<Metric>(getMetric("c"))
    const [z, setZ] = useState<Metric>(getMetric("a"))

    let content: JSX.Element
    switch (state) {
        case ComponentState.LOADING:
            content = <LoadingIndicator />
            break
        case ComponentState.ERROR:
            content = (
                <>
                    <Error />
                    Failed to load records
                </>
            )
            break
        case ComponentState.READY:
            content = <SizedPuzzleFrontierPlot records={records} mode={mode} x={x} y={y} z={z} />
            break
    }

    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: "row",
                height: "100%",
            }}
        >
            <PuzzleFrontierConfiguration mode={mode} setMode={setMode} x={x} setX={setX} y={y} setY={setY} z={z} setZ={setZ} />
            {content}
        </Box>
    )
}
