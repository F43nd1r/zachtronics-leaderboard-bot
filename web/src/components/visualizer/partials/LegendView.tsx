/*
 * Copyright (c) 2022
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

import { Box, Stack } from "@mui/material"
import { FiberManualRecord, FiberManualRecordOutlined } from "@mui/icons-material"
import FieldSet from "../../FieldSet"
import Modifier from "../../../model/Modifier"
import iterate from "../../../utils/iterate"

interface LegendViewProps {
    modifiers: Record<string, Modifier<any>>
    defaultColor: string
}

export function LegendView(props: LegendViewProps) {
    return (
        <FieldSet title="Legend">
            <Stack spacing={1}>
                {iterate(props.modifiers)
                    .map(([_, modifier]) => modifier)
                    .concat({
                        name: "normal",
                        color: props.defaultColor,
                        legendOrder: 0,
                        option1: "Normal",
                        option2: "never",
                        get: () => undefined,
                    })
                    .sort((modifier) => modifier.legendOrder)
                    .map((modifier) => (
                        <LegendItem key={modifier.name} label={modifier.option1} color={modifier.color} filled={true} />
                    ))}
                <LegendItem label={"Not on the selected frontier"} color={"#888888"} filled={false} />
            </Stack>
        </FieldSet>
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
                marginTop: "0.5rem",
            }}
        >
            {props.filled ? <FiberManualRecord htmlColor={props.color} /> : <FiberManualRecordOutlined htmlColor={props.color} />}
            <span>{props.label}</span>
        </Box>
    )
}
