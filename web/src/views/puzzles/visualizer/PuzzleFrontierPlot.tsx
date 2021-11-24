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

import { useTheme } from "@mui/material"
import { ComponentType, useState } from "react"
import OmRecord from "../../../model/Record"
import { Axis } from "plotly.js"
import { PlotParams } from "react-plotly.js"
import createPlotlyComponent from "react-plotly.js/factory"
import Plotly from "plotly.js-gl3d-dist-min"
import { Metric } from "../../../model/Metric"
import { Configuration } from "./Configuration"
import { RecordModal } from "../../../fragments/RecordModal"
import { withSize } from "react-sizeme"

export interface PuzzleFrontierPlotProps {
    size: { width: number; height: number }
    records: OmRecord[]
    configuration: Configuration
}

const Plot: ComponentType<PlotParams> = createPlotlyComponent(Plotly)

function isStrictlyBetterInMetrics(r1: OmRecord, r2: OmRecord, metrics: Metric[]): boolean {
    const compares = metrics.map((metric) => (metric.get(r1) ?? Number.MAX_SAFE_INTEGER) - (metric.get(r2) ?? Number.MAX_SAFE_INTEGER))
    return !compares.some((compare) => compare > 0) && compares.some((compare) => compare < 0)
}

function getColor(record: OmRecord) {
    return record.score?.overlap ? "#880e4f" : record.score?.trackless ? "#558b2f" : "#0288d1"
}

function buildInnerColorGetter(records: OmRecord[], metrics: Metric[]) {
    return (record: OmRecord) => (records.some((r) => isStrictlyBetterInMetrics(r, record, metrics)) ? "#00000000" : getColor(record))
}

function PuzzleFrontierPlot(props: PuzzleFrontierPlotProps) {
    const theme = useTheme()

    const [activeRecord, setActiveRecord] = useState<OmRecord | undefined>(undefined)
    const configuration = props.configuration
    const records = props.records.filter((record: OmRecord) => {
        return (
            (configuration.filter.overlap === undefined || configuration.filter.overlap === record.score?.overlap) &&
            (configuration.filter.trackless === undefined || configuration.filter.trackless === record.score?.trackless)
        )
    })

    const gridColor = theme.palette.mode === "light" ? theme.palette.grey["200"] : theme.palette.grey["800"]

    const makeAxis = (metric: Metric): Partial<Axis> => {
        return {
            title: metric.name,
            color: theme.palette.text.primary,
            gridcolor: gridColor,
            zerolinecolor: gridColor,
            rangemode: "tozero",
        }
    }

    return (
        <>
            <Plot
                data={[
                    configuration.mode === "2D"
                        ? {
                              x: records.map((record) => configuration.x.get(record) ?? 0),
                              y: records.map((record) => configuration.y.get(record) ?? 0),
                              hovertext: records.map((record) => record.fullFormattedScore ?? "none"),
                              hovertemplate: `${configuration.x.name}: %{x}<br>${configuration.y.name}: %{y}<br>%{hovertext}<extra></extra>`,
                              type: "scatter",
                              mode: "markers",
                              marker: {
                                  size: 20,
                                  color: records.map(buildInnerColorGetter(records, [configuration.x, configuration.y])),
                                  line: {
                                      width: 3,
                                      color: records.map(getColor),
                                  },
                              },
                          }
                        : {
                              x: records.map((record) => configuration.x.get(record) ?? 0),
                              y: records.map((record) => configuration.y.get(record) ?? 0),
                              z: records.map((record) => configuration.z.get(record) ?? 0),
                              hovertext: records.map((record) => record.fullFormattedScore ?? "none"),
                              hovertemplate: `${configuration.x.name}: %{x}<br>${configuration.y.name}: %{y}<br>${configuration.z.name}: %{z}<br>%{hovertext}<extra></extra>`,
                              type: "scatter3d",
                              mode: "markers",
                              marker: {
                                  size: 10,
                                  color: records.map(buildInnerColorGetter(records, [configuration.x, configuration.y, configuration.z])),
                                  line: {
                                      width: 2,
                                      color: records.map(getColor),
                                  },
                              },
                          },
                ]}
                layout={{
                    autosize: false,
                    width: props.size.width ?? undefined,
                    height: props.size.height ?? undefined,
                    paper_bgcolor: "transparent",
                    plot_bgcolor: "transparent",
                    xaxis: makeAxis(configuration.x),
                    yaxis: makeAxis(configuration.y),
                    hoverlabel: {
                        bgcolor: theme.palette.background.paper,
                    },
                    scene: {
                        xaxis: makeAxis(configuration.x),
                        yaxis: makeAxis(configuration.y),
                        zaxis: makeAxis(configuration.z),
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
                    const record = props.records.find(
                        (record) => configuration.x.get(record) === point.x && configuration.y.get(record) === point.y && (configuration.mode === "2D" || configuration.z.get(record) === point.z)
                    )
                    setActiveRecord(record)
                }}
            />
            <RecordModal record={activeRecord} setRecord={setActiveRecord} />
        </>
    )
}

const SizedPuzzleFrontierPlot = withSize({ monitorHeight: true })<PuzzleFrontierPlotProps>(PuzzleFrontierPlot)

export default SizedPuzzleFrontierPlot
