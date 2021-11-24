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

import { Box } from "@mui/material"
import { FiberManualRecord, FiberManualRecordOutlined } from "@mui/icons-material"

export function Legend() {
    return (
        <>
            <LegendItem label={"Overlap"} color={"#880e4f"} filled={true} />
            <LegendItem label={"Normal"} color={"#0288d1"} filled={true} />
            <LegendItem label={"Trackless"} color={"#558b2f"} filled={true} />
            <LegendItem label={"Not on the selected frontier"} color={"#888888"} filled={false} />
        </>
    )
}

interface LegendItemProps {
    color: string
    filled: boolean
    label: string
}

function LegendItem(props: LegendItemProps) {
    return (
        <Box
            sx={{
                display: "flex",
                marginTop: "1rem",
            }}
        >
            {props.filled ? <FiberManualRecord htmlColor={props.color} /> : <FiberManualRecordOutlined htmlColor={props.color} />}
            <span>{props.label}</span>
        </Box>
    )
}
