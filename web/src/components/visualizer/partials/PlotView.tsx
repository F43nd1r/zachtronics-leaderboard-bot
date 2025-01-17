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
import { ComponentType } from "react"
import RecordDTO, { isStrictlyBetterInMetrics } from "../../../model/RecordDTO"
import { Axis, PlotData } from "plotly.js"
import { PlotParams } from "react-plotly.js"
import createPlotlyComponent from "react-plotly.js/factory"
import Plotly from "plotly.js-gl3d-dist-min"
import Metric from "../../../model/Metric"
import { Configuration, MetricConfiguration } from "../../../model/Configuration"
import { applyFilter, Filter } from "../../../model/Filter"
import Modifier from "../../../model/Modifier"
import iterate from "../../../utils/iterate"
import useDimensions from "react-cool-dimensions"

export interface PlotViewProps<SCORE, RECORD extends RecordDTO<SCORE>> {
    metrics: Record<string, Metric<SCORE>>
    modifiers: Record<string, Modifier<SCORE>>
    defaultColor: string
    records: RECORD[]
    configuration: Configuration
    filter: Filter
    onClick: (records: RECORD[]) => void
}

const Plot: ComponentType<PlotParams> = createPlotlyComponent(Plotly)

function findMetric<SCORE>(metrics: Record<string, Metric<SCORE>>, configuration: MetricConfiguration) {
    return { metric: metrics[configuration.metric] ?? Object.values(metrics)[0], scale: configuration.scale }
}

function SizeAwarePlotView<SCORE, RECORD extends RecordDTO<SCORE>>({
    configuration,
    metrics,
    modifiers,
    defaultColor,
    size,
    records: recordsIn,
    filter,
    onClick,
}: PlotViewProps<SCORE, RECORD> & { size: { width?: number; height?: number } }) {
    const records = applyFilter(metrics, modifiers, filter, configuration, recordsIn)
    const x = findMetric(metrics, configuration.x)
    const y = findMetric(metrics, configuration.y)
    const z = findMetric(metrics, configuration.z)
    const getPointString = (record: RecordDTO<SCORE>): string =>
        JSON.stringify({
            x: x.metric.get(record.score) ?? 0,
            y: y.metric.get(record.score) ?? 0,
            z: configuration.mode === "3D" ? z.metric.get(record.score) : undefined,
        })
    const recordMap = records.reduce(
        (acc, record) => {
            const point = getPointString(record)
            return { ...acc, [point]: [...(acc[point] ?? []), record] }
        },
        {} as Record<string, RECORD[]>,
    )

    const theme = useTheme()
    const gridColor = theme.palette.mode === "light" ? theme.palette.grey["200"] : theme.palette.grey["800"]
    const makeAxis = (axis: { metric: Metric<SCORE>; scale: "linear" | "log" }): Partial<Axis> => {
        return {
            title: axis.metric.name,
            color: theme.palette.text.primary,
            gridcolor: gridColor,
            zerolinecolor: gridColor,
            rangemode: "tozero",
            type: axis.scale,
            tickformat: "s",
        }
    }

    const getColor = (record: RecordDTO<SCORE>) => {
        const modifier = iterate(modifiers)
            .map(([_, modifier]) => modifier)
            .find((modifier) => modifier.get(record.score))
        return modifier ? modifier.color : defaultColor
    }
    const getMarkerColors = (...metrics: Metric<SCORE>[]) =>
        records.map((record) => (records.some((r) => isStrictlyBetterInMetrics(r, record, metrics)) ? theme.palette.background.paper : getColor(record)))

    const common: Partial<PlotData> = {
        hovertext: records.map(
            (r: RecordDTO<SCORE>) =>
                "-----<br>" +
                recordMap[getPointString(r)]
                    .map(
                        (record) =>
                            `${record.fullFormattedScore ?? "none"}${record.author ? "<br>by " + record.author : ""}${
                                record.smartFormattedCategories ? "<br>" + record.smartFormattedCategories : ""
                            }`,
                    )
                    .join("<br>-----<br>"),
        ),
        x: records.map((record) => x.metric.get(record.score) ?? 0),
        y: records.map((record) => y.metric.get(record.score) ?? 0),
        mode: "markers",
        marker: {
            line: {
                color: records.map(getColor),
            },
        },
        ids: records.map((record) => record.fullFormattedScore ?? "none"),
    }

    return (
        <>
            <Plot
                key={configuration.mode}
                data={[
                    configuration.mode === "2D"
                        ? {
                              ...common,
                              hovertemplate: `${x.metric.name}: %{x:.3~f}<br>${y.metric.name}: %{y:.3~f}<br>%{hovertext}<extra></extra>`,
                              type: "scatter",
                              marker: {
                                  ...common.marker,
                                  size: 20,
                                  color: getMarkerColors(x.metric, y.metric),
                                  line: {
                                      ...common.marker?.line,
                                      width: 3,
                                  },
                              },
                          }
                        : {
                              ...common,
                              z: records.map((record) => z.metric.get(record.score) ?? 0),
                              hovertemplate: `${x.metric.name}: %{x:.3~f}<br>${y.metric.name}: %{y:.3~f}<br>${z.metric.name}: %{z:.3~f}<br>%{hovertext}<extra></extra>`,
                              type: "scatter3d",
                              marker: {
                                  ...common.marker,
                                  size: 10,
                                  color: getMarkerColors(x.metric, y.metric, z.metric),
                                  line: {
                                      ...common.marker?.line,
                                      width: 2,
                                  },
                              },
                          },
                ]}
                layout={{
                    autosize: false,
                    width: size.width ?? undefined,
                    height: size.height ?? undefined,
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
                    transition: {
                        duration: 500,
                        easing: "cubic-in-out",
                    },
                    uirevision: JSON.stringify(filter) + JSON.stringify(configuration),
                }}
                config={{
                    displaylogo: false,
                }}
                style={{
                    minWidth: 0,
                    width: "100%",
                    minHeight: 0,
                    height: "100%",
                    flexGrow: 1,
                }}
                useResizeHandler={true}
                onClick={(event) => {
                    onClick(recordMap[getPointString(records[event.points[0].pointNumber])])
                }}
            />
        </>
    )
}

export default function PlotView<SCORE, RECORD extends RecordDTO<SCORE>>(props: PlotViewProps<SCORE, RECORD>) {
    const { observe, width, height } = useDimensions()
    return (
        <div ref={observe} style={{ minWidth: 0, flex: 1 }}>
            <SizeAwarePlotView size={{ width, height }} {...props} />
        </div>
    )
}
