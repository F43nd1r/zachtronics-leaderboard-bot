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

import { Grid } from "@mui/material"
import Record from "../model/Record"
import RecordCard from "./RecordCard"

interface RecordGridProps {
    records: Record[]
    getTitle: (record: Record) => string
    getScore: (record: Record) => string
}

export default function RecordGrid(props: RecordGridProps) {
    return (
        <Grid container spacing={3}>
            {props.records.map((record, index) => (
                <Grid item xs key={index}>
                    <RecordCard record={record} title={props.getTitle(record)} score={props.getScore(record)} />
                </Grid>
            ))}
        </Grid>
    )
}
