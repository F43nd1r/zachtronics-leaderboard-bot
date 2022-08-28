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

import { Card, Modal } from "@mui/material"
import RecordCard, { RecordCardProps } from "./RecordCard"
import RecordDTO from "../model/RecordDTO"
import { FC } from "react"

export interface RecordModalProps<RECORD extends RecordDTO<any>> {
    records?: RECORD[]
    setRecords: (records: RECORD[] | undefined) => void
    RecordCardComponent?: FC<RecordCardProps<RECORD>>
}

export function RecordModal<RECORD extends RecordDTO<any>>({ records, setRecords, RecordCardComponent = RecordCard }: RecordModalProps<RECORD>) {
    return (
        <Modal open={records !== undefined} onClose={() => setRecords(undefined)}>
            <Card
                sx={{
                    position: "absolute",
                    top: "50%",
                    left: "50%",
                    transform: "translate(-50%, -50%)",
                    boxShadow: 24,
                    outline: 0,
                    display: "flex",
                    paddingLeft: "1.5rem",
                    paddingRight: "1.5rem",
                    gap: "2rem",
                }}
            >
                {records?.map((record) => (
                    <RecordCardComponent record={record!} title={record!.smartFormattedCategories || "Pareto Frontier"} score={record!.fullFormattedScore ?? "None"} />
                ))}
            </Card>
        </Modal>
    )
}
