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

import {
    Box,
    Button,
    Card,
    CardActionArea,
    CardActions,
    CardContent,
    CardHeader,
    CardMedia,
    Link as ExternalLink,
    useTheme
} from "@mui/material"
import { SentimentNeutral, SentimentVeryDissatisfied } from "@mui/icons-material"
import RecordDTO from "../model/RecordDTO"
import { ReactNode } from "react"
import { AppSettings, useAppSettings } from "../fragments/AppSettingsProvider"
import { useInView } from "react-intersection-observer"
import { Link } from "react-router-dom"

export interface RecordCardProps<RECORD extends RecordDTO<any> = RecordDTO<any>> {
    record: RECORD
    title: ReactNode
    score: ReactNode
    additionalActions?: ReactNode
}

function isYoutube(url: string): boolean {
    return url.includes("youtu.be") || url.includes("youtube.com")
}

function isImgurGif(url: string): boolean {
    return url.includes("imgur.com") && (url.endsWith(".gif") || url.endsWith(".gifv"))
}

function isMorsTech(url: string): boolean {
    return url.includes("mors.technology")
}

function getMediaType(url: string): "video" | "iframe" | "img" {
    if (url.endsWith(".mp4") || url.endsWith(".webm") || isImgurGif(url) || isMorsTech(url)) {
        return "video"
    } else if (isYoutube(url)) {
        return "iframe"
    } else {
        // imgur albums land here, they will not embed
        return "img"
    }
}

function getMediaUrl(gif: string, appSettings: AppSettings) {
    if (isYoutube(gif)) {
        const url = new URL(gif)
        let id = url.pathname.substring(1)
        if (id === "watch") id = url.searchParams.get("v")!

        let result = `https://youtube.com/embed/${id}?playlist=${id}&autoplay=${appSettings.autoPlay ? 1 : 0}&loop=1&controls=${appSettings.showControls ? 1 : 0}`
        if (url.searchParams.has("t")) result += "&start=" + url.searchParams.get("t")
        return result
    } else if (isImgurGif(gif)) {
        return gif.replace(/.+\/(\w+)(\..*)?/, "https://i.imgur.com/$1.mp4")
    } else if (isMorsTech(gif)) {
        return gif.replace(/.+mors\.technology\/(.+)(\..*)/, "https://files.mors.technology/$1.mp4")
    } else {
        return gif
    }
}

export default function RecordCard(props: RecordCardProps) {
    const appSettings = useAppSettings()
    const theme = useTheme()
    const { ref, inView } = useInView({ rootMargin: "100%", triggerOnce: true })
    return (
        <Card
            ref={ref}
            sx={{
                minWidth: "min(90vw, 460px)",
                maxWidth: "100%",
            }}
        >
            <CardHeader title={props.title} />
            {props.record.gif && appSettings.showMedia ? (
                <CardActionArea
                    href={props.record.gif ?? ""}
                    target={"_blank"}
                    rel={"noreferrer"}
                    sx={{
                        width: "fit-content",
                        marginLeft: "auto",
                        marginRight: "auto",
                    }}
                >
                    <CardMedia
                        component={getMediaType(props.record.gif)}
                        autoPlay={appSettings.autoPlay}
                        controls={appSettings.showControls}
                        loop
                        muted
                        loading="lazy"
                        playsInline
                        src={inView ? getMediaUrl(props.record.gif, appSettings) : undefined}
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
            ) : props.record.gif ? (
                <Box
                    sx={{
                        display: "flex",
                        justifyContent: "center",
                        alignItems: "center",
                        height: "2.5rem",
                        width: "100%",
                        gap: 0.5,
                    }}
                >
                    View the{" "}
                    <ExternalLink href={props.record.gif} style={{ color: theme.palette.primary.main }}>
                        media
                    </ExternalLink>.
                    Enable gif preview (<Link to="/settings" style={{ color: theme.palette.primary.main }}>settings</Link>).
                </Box>
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
                <Button
                    component={ExternalLink}
                    href={props.record.solution}
                    download
                    size="small"
                    variant="outlined"
                    color="primary"
                    disabled={!props.record.solution}
                    sx={{ color: "primary.main", textDecoration: "none" }}
                >
                    Download
                </Button>
                {props.additionalActions}
            </CardActions>
        </Card>
    )
}
