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

import RecordCard, { RecordCardProps } from "./RecordCard"
import { Button } from "@mui/material"
import { fetchFromApiRaw } from "../utils/fetchFromApi"
import { OmRecord } from "../model/om/OmRecord"

export default function OmRecordCard(props: RecordCardProps<OmRecord>) {
    const omCloneUrl = "https://ssk97.github.io/display/demo.html"
    return (
        <RecordCard
            {...props}
            additionalActions={
                <Button size="small" variant="outlined" color="primary" disabled={!props.record.solution}>
                    <a
                        href={omCloneUrl}
                        target={"_blank"}
                        rel={"noreferrer"}
                        onClick={function (event) {
                            event.preventDefault()
                            const w = window.open(omCloneUrl)
                            let solution: Uint8Array
                            let puzzle: Uint8Array
                            let ready = false

                            function loadPreview() {
                                w!.postMessage({ command: "load", puzzle, solution }, "*")
                            }

                            window.addEventListener("message", (e) => {
                                if (e.origin !== new URL(omCloneUrl).origin) return
                                if (solution && puzzle) loadPreview()
                                else ready = true
                            })
                            fetch(props.record.solution!, { headers: { "Access-Control-Allow-Origin": "*" } })
                                .then((response) => {
                                    console.log(response.status)
                                    return response.arrayBuffer()
                                })
                                .then((solutionData) => {
                                    solution = new Uint8Array(solutionData)
                                    fetchFromApiRaw(`/om/puzzle/${props.record.puzzle.id}/file`)
                                        .then((response) => response.arrayBuffer())
                                        .then((puzzleData) => {
                                            puzzle = new Uint8Array(puzzleData)
                                            if (ready) loadPreview()
                                        })
                                })
                        }}
                        style={{
                            color: "inherit",
                            textDecoration: "none",
                        }}
                    >
                        Preview
                    </a>
                </Button>
            }
        />
    )
}
