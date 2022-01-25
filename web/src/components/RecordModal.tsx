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

import { Box, Modal } from "@mui/material"
import RecordCard from "./RecordCard"
import RecordDTO from "../model/RecordDTO"

interface RecordModalProps {
    record?: RecordDTO<any>
    setRecord: (record: RecordDTO<any> | undefined) => void
}

export function RecordModal(props: RecordModalProps) {
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
                    score={props.record?.fullFormattedScore ?? "None"}
                    sx={{
                        paddingLeft: "1rem",
                        paddingRight: "1rem",
                    }}
                />
            </Box>
        </Modal>
    )
}
