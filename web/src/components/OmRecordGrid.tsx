/*
 * Copyright (c) 2026
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

import {Box, Grid} from "@mui/material"
import OmRecordCard from "./OmRecordCard"
import {OmRecord} from "../model/om/OmRecord"
import {ReactNode} from "react"

interface RecordGridProps {
    records: OmRecord[]
    getTitle?: (record: OmRecord) => ReactNode
    getScore: (record: OmRecord) => string
}

const metricDescriptions: Record<string, string> = {
    G: "Cost",
    C: "Cycles",
    A: "Area",
    I: "Instructions",
    B: "Bounding Hexagon",
    H: "Height",
    W: "Width",
    R: "Rate",
    X: "Product",
    Sum: "Sum (Cost + Cycles + Area)",
    Sum4: "Sum4 (Cost + Cycles + Area + Instructions)",
}

function getCategoryElement(category: string): ReactNode {
    const originalCategory = category;
    const rootTitleElements = [];
    const sum = category.includes("Sum");

    if (sum) {
        rootTitleElements.push(metricDescriptions[category] || category);
        category = category.replaceAll("Sum4", "").replaceAll("Sum", "");
    }

    category = category.replaceAll("O", "").replaceAll("L", "").replaceAll("T", "");
    category
        .split("")
        .forEach(letter => rootTitleElements.push(metricDescriptions[letter] ?? letter));

    if (rootTitleElements.length > 1) {
        rootTitleElements[0] += " primary";
        rootTitleElements[1] += " secondary";
    }
    if (rootTitleElements.length > 2) {
        rootTitleElements[2] += " tertiary";
    }
    if (originalCategory.includes("O")) {
        rootTitleElements.push("overlapping");
    }
    if (originalCategory.includes("L")) {
        rootTitleElements.push("looping");
    }
    if (originalCategory.includes("T")) {
        rootTitleElements.push("trackless");
    }

    return (
        <span title={rootTitleElements.join(", ")}>
            {originalCategory}
        </span>
    );
}

function getDefaultTitle(record: OmRecord): ReactNode {
    if (!record.smartFormattedCategories) {
        return <Box>Pareto Frontier</Box>;
    }
    return (
        <Box>
            {record.smartFormattedCategories
                .split(/,\s*/)
                .map(getCategoryElement)
                .reduce((previous, current) => [previous, ", ", current])
            }
        </Box>
    );
}

export default function OmRecordGrid(props: RecordGridProps) {
    return (
        <Grid container spacing={3}>
            {props.records.map((record, index) => (
                <Grid key={index}>
                    <OmRecordCard record={record} title={(props.getTitle ?? getDefaultTitle)(record)} score={props.getScore(record)} />
                </Grid>
            ))}
        </Grid>
    )
}
