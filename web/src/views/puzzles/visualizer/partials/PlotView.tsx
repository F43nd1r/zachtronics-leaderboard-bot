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
import OmRecord from "../../../../model/Record"
import { Axis, PlotData, Point } from "plotly.js"
import { PlotParams } from "react-plotly.js"
import createPlotlyComponent from "react-plotly.js/factory"
import Plotly from "plotly.js-gl3d-dist-min"
import { getMetric, Metric } from "../../../../model/Metric"
import { Configuration } from "../../../../model/Configuration"
import { RecordModal } from "../../../../fragments/RecordModal"
import { withSize } from "react-sizeme"
import { applyFilter, Filter } from "../../../../model/Filter"

export interface PlotViewProps {
    size: { width: number; height: number }
    records: OmRecord[]
    configuration: Configuration
    filter: Filter
}

const Plot: ComponentType<PlotParams> = createPlotlyComponent(Plotly)

function isStrictlyBetterInMetrics(r1: OmRecord, r2: OmRecord, metrics: Metric[]): boolean {
    const compares = metrics.map((metric) => (metric.get(r1) ?? Number.MAX_SAFE_INTEGER) - (metric.get(r2) ?? Number.MAX_SAFE_INTEGER))
    return !compares.some((compare) => compare > 0) && compares.some((compare) => compare < 0)
}

function getColor(record: OmRecord) {
    return record.score?.overlap ? "#880e4f" : record.score?.trackless ? "#558b2f" : "#0288d1"
}

function PlotView(props: PlotViewProps) {
    const [activeRecord, setActiveRecord] = useState<OmRecord | undefined>(undefined)

    const records = applyFilter(props.filter, props.records)
    const configuration = props.configuration
    const x = getMetric(configuration.x)
    const y = getMetric(configuration.y)
    const z = getMetric(configuration.z)

    const theme = useTheme()
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

    const common: Partial<PlotData> = {
        hovertext: records.map((record: OmRecord) => `${record.fullFormattedScore ?? "none"}<br>${record.smartFormattedCategories}`),
        x: records.map((record) => x.get(record) ?? 0),
        y: records.map((record) => y.get(record) ?? 0),
        mode: "markers",
        marker: {
            color: records.map((record: OmRecord) => (records.some((r) => isStrictlyBetterInMetrics(r, record, [x, y])) ? "#00000000" : getColor(record))),
            line: {
                color: records.map(getColor),
            },
        },
    }
    return (
        <>
            <Plot
                data={[
                    configuration.mode === "2D"
                        ? {
                              ...common,
                              hovertemplate: `${x.name}: %{x}<br>${y.name}: %{y}<br>%{hovertext}<extra></extra>`,
                              type: "scatter",
                              marker: {
                                  ...common.marker,
                                  size: 20,
                                  line: {
                                      ...common.marker?.line,
                                      width: 3,
                                  },
                              },
                          }
                        : {
                              ...common,
                              z: records.map((record) => z.get(record) ?? 0),
                              hovertemplate: `${x.name}: %{x}<br>${y.name}: %{y}<br>${z.name}: %{z}<br>%{hovertext}<extra></extra>`,
                              type: "scatter3d",
                              marker: {
                                  ...common.marker,
                                  size: 10,
                                  line: {
                                      ...common.marker?.line,
                                      width: 2,
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
                    xaxis: makeAxis(x),
                    yaxis: makeAxis(y),
                    hoverlabel: {
                        bgcolor: theme.palette.background.paper,
                    },
                    scene: {
                        xaxis: makeAxis(x),
                        yaxis: makeAxis(y),
                        zaxis: makeAxis(z),
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
                    const point = event.points[0] as Partial<Point>
                    const record = props.records.find((record) => x.get(record) === point.x && y.get(record) === point.y && (configuration.mode === "2D" || z.get(record) === point.z))
                    setActiveRecord(record)
                }}
            />
            <RecordModal record={activeRecord} setRecord={setActiveRecord} />
        </>
    )
}

const SizedPuzzleFrontierPlot = withSize({ monitorHeight: true })<PlotViewProps>(PlotView)

export default SizedPuzzleFrontierPlot
