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

import { Box, Button, Card, CardActionArea, CardActions, CardContent, CardHeader, CardMedia } from "@mui/material"
import { SentimentVeryDissatisfied } from "@mui/icons-material"
import RecordDTO from "../model/RecordDTO"
import { ReactNode } from "react"

interface RecordCardProps {
    record: RecordDTO<any>
    title: string
    score: ReactNode
}

function isYoutube(gif: string): boolean {
    return gif.includes("youtu.be") || gif.includes("youtube.com")
}

function isImgur(gif: string): boolean {
    return gif.includes("imgur")
}

function getVideoType(gif: string): "video" | "iframe" | "img" {
    if (gif.endsWith(".mp4") || gif.endsWith(".webm") || isImgur(gif)) {
        return "video"
    } else if (isYoutube(gif)) {
        return "iframe"
    } else {
        return "img"
    }
}

function getVideoUrl(gif: string) {
    if (isYoutube(gif)) {
        return gif.replace(/.+\/(\w+)(?:\\?.+)?$/, "https://youtube.com/embed/$1")
    } else if (isImgur(gif)) {
        return gif.replace(/.+\/(\w+)(\..*)?/, "https://i.imgur.com/$1.mp4")
    } else {
        return gif
    }
}

export default function RecordCard(props: RecordCardProps) {
    return (
        <Card
            sx={{
                minWidth: "min(90vw, 460px)",
                maxWidth: "100%",
            }}
        >
            <CardHeader title={props.title} />
            {props.record.gif ? (
                <CardActionArea
                    href={props.record.gif ?? ""}
                    sx={{
                        width: "fit-content",
                        marginLeft: "auto",
                        marginRight: "auto",
                    }}
                >
                    <CardMedia
                        component={getVideoType(props.record.gif)}
                        autoPlay
                        loop
                        muted
                        src={getVideoUrl(props.record.gif)}
                        alt="Media not loading"
                        style={{
                            height: "min(70vh, 360px)",
                            width: isYoutube(props.record.gif) ? "min(70vw, 720px)" : "auto",
                            maxWidth: "100%",
                            lineHeight: "50px",
                            textAlign: "center",
                        }}
                    />
                </CardActionArea>
            ) : (
                <Box
                    sx={{
                        display: "flex",
                        justifyContent: "center",
                        alignItems: "center",
                        height: "360px",
                        width: "480px",
                        marginLeft: "auto",
                        marginRight: "auto",
                        flexDirection: "column",
                    }}
                >
                    <SentimentVeryDissatisfied sx={{ margin: "1rem" }} fontSize="large" />
                    <span>No media found</span>
                </Box>
            )}
            <CardContent sx={{ userSelect: "text" }}>{props.score}</CardContent>

            <CardActions
                style={{
                    padding: "1rem",
                    display: "flex",
                    justifyContent: "end",
                }}
            >
                <Button size="small" variant="outlined" color="primary" disabled={!props.record.solution}>
                    <a
                        href={props.record.solution}
                        download
                        style={{
                            color: "inherit",
                            textDecoration: "none",
                        }}
                    >
                        Download
                    </a>
                </Button>
            </CardActions>
        </Card>
    )
}
