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
import RecordDTO, { isStrictlyBetterInMetrics } from "../../../model/RecordDTO"
import { Axis, PlotData } from "plotly.js"
import { PlotParams } from "react-plotly.js"
import createPlotlyComponent from "react-plotly.js/factory"
import Plotly from "plotly.js-gl3d-dist-min"
import Metric from "../../../model/Metric"
import { Configuration } from "../../../model/Configuration"
import { RecordModal } from "../../RecordModal"
import { SizeMe, SizeMeProps } from "react-sizeme"
import { applyFilter, Filter } from "../../../model/Filter"
import Modifier from "../../../model/Modifier"
import iterate from "../../../utils/iterate"

export interface PlotViewProps<MODIFIER_ID extends string, METRIC_ID extends string, SCORE> {
    metrics: Record<METRIC_ID, Metric<SCORE>>
    modifiers: Record<MODIFIER_ID, Modifier<SCORE>>
    defaultColor: string
    records: RecordDTO<SCORE>[]
    configuration: Configuration<METRIC_ID>
    filter: Filter<MODIFIER_ID, METRIC_ID>
}

const Plot: ComponentType<PlotParams> = createPlotlyComponent(Plotly)

function SizeAwarePlotView<MODIFIER_ID extends string, METRIC_ID extends string, SCORE>(props: PlotViewProps<MODIFIER_ID, METRIC_ID, SCORE> & SizeMeProps) {
    const [activeRecords, setActiveRecords] = useState<RecordDTO<SCORE>[] | undefined>(undefined)

    const configuration = props.configuration
    const records = applyFilter(props.metrics, props.modifiers, props.filter, configuration, props.records)
    const x = props.metrics[configuration.x]
    const y = props.metrics[configuration.y]
    const z = props.metrics[configuration.z]
    const getPointString = (record: RecordDTO<SCORE>): string =>
        JSON.stringify({
            x: x.get(record.score) ?? 0,
            y: y.get(record.score) ?? 0,
            z: configuration.mode === "3D" ? z.get(record.score) : undefined,
        })
    const recordMap = records.reduce((acc, record) => {
        const point = getPointString(record)
        return { ...acc, [point]: [...(acc[point] ?? []), record] }
    }, {} as Record<string, RecordDTO<SCORE>[]>)

    const theme = useTheme()
    const gridColor = theme.palette.mode === "light" ? theme.palette.grey["200"] : theme.palette.grey["800"]
    const makeAxis = (metric: Metric<SCORE>): Partial<Axis> => {
        return {
            title: metric.name,
            color: theme.palette.text.primary,
            gridcolor: gridColor,
            zerolinecolor: gridColor,
            rangemode: "tozero",
        }
    }

    const getColor = (record: RecordDTO<SCORE>) => {
        const modifier = iterate(props.modifiers)
            .map(([_, modifier]) => modifier)
            .find((modifier) => modifier.get(record.score))
        return modifier ? modifier.color : props.defaultColor
    }
    const getMarkerColors = (...metrics: Metric<SCORE>[]) => records.map((record) => (records.some((r) => isStrictlyBetterInMetrics(r, record, metrics)) ? "#000000" : getColor(record)))

    const common: Partial<PlotData> = {
        hovertext: records.map(
            (r: RecordDTO<SCORE>) =>
                "-----<br>" +
                recordMap[getPointString(r)]
                    .map(
                        (record) =>
                            `${record.fullFormattedScore ?? "none"}${record.author ? "<br>by " + record.author : ""}${record.smartFormattedCategories ? "<br>" + record.smartFormattedCategories : ""}`,
                    )
                    .join("<br>-----<br>"),
        ),
        x: records.map((record) => x.get(record.score) ?? 0),
        y: records.map((record) => y.get(record.score) ?? 0),
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
                              hovertemplate: `${x.name}: %{x:.3~f}<br>${y.name}: %{y:.3~f}<br>%{hovertext}<extra></extra>`,
                              type: "scatter",
                              marker: {
                                  ...common.marker,
                                  size: 20,
                                  color: getMarkerColors(x, y),
                                  line: {
                                      ...common.marker?.line,
                                      width: 3,
                                  },
                              },
                          }
                        : {
                              ...common,
                              z: records.map((record) => z.get(record.score) ?? 0),
                              hovertemplate: `${x.name}: %{x:.3~f}<br>${y.name}: %{y:.3~f}<br>${z.name}: %{z:.3~f}<br>%{hovertext}<extra></extra>`,
                              type: "scatter3d",
                              marker: {
                                  ...common.marker,
                                  size: 10,
                                  color: getMarkerColors(x, y, z),
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
                    transition: {
                        duration: 500,
                        easing: "cubic-in-out",
                    },
                    uirevision: "true",
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
                    setActiveRecords(recordMap[getPointString(records[event.points[0].pointNumber])])
                }}
            />
            <RecordModal records={activeRecords} setRecords={setActiveRecords} />
        </>
    )
}

export default function PlotView<MODIFIER_ID extends string, METRIC_ID extends string, SCORE>(props: PlotViewProps<MODIFIER_ID, METRIC_ID, SCORE>) {
    return (
        <SizeMe monitorHeight={true} monitorWidth={true}>
            {({ size }) => <SizeAwarePlotView size={size} {...props} />}
        </SizeMe>
    )
}
