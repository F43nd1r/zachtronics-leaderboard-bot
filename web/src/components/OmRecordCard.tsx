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
                props.record.solution && !props.record.score?.overlap && (props.record.puzzle as any).type !== "PRODUCTION" ? (
                    <Button size="small" variant="outlined" color="primary">
                        <a
                            href={omCloneUrl}
                            target={"_blank"}
                            rel={"noreferrer"}
                            onClick={async function (event) {
                                event.preventDefault()
                                const w = window.open(omCloneUrl)

                                if (w) {
                                    const [solution, puzzle] = await Promise.all([
                                        fetchFromApiRaw(`/om/puzzle/${props.record.puzzle.id}/record/${props.record.id}/file`)
                                            .then((response) => response.arrayBuffer())
                                            .then((buffer) => new Uint8Array(buffer)),
                                        fetchFromApiRaw(`/om/puzzle/${props.record.puzzle.id}/file`)
                                            .then((response) => response.arrayBuffer())
                                            .then((buffer) => new Uint8Array(buffer)),
                                        new Promise((resolve) => {
                                            window.addEventListener("message", (e) => {
                                                if (e.origin !== new URL(omCloneUrl).origin) return
                                                resolve(true)
                                            })
                                        }),
                                    ])
                                    w.postMessage({ command: "load", puzzle, solution }, "*")
                                }
                            }}
                            style={{
                                color: "inherit",
                                textDecoration: "none",
                            }}
                        >
                            Preview
                        </a>
                    </Button>
                ) : undefined
            }
        />
    )
}
