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

import { Box, Button, Card, CardActionArea, CardActions, CardContent, CardHeader, CardMedia, Grid } from "@mui/material"
import { SentimentVeryDissatisfied } from "@mui/icons-material"
import React from "react"
import Record from "../model/Record"

interface RecordGridProps {
    records: Record[]
    getTitle: (record: Record) => string
}

export default function RecordGrid(props: RecordGridProps) {
    return (
        <Grid container spacing={3}>
            {props.records.map((record) => (
                <Grid item xs>
                    <Card
                        sx={{
                            minWidth: "min(100vw, 460px)",
                            maxWidth: "100%",
                        }}
                    >
                        <CardActionArea href={record.gif ?? window.location.href}>
                            <CardHeader title={props.getTitle(record)} />
                            {record.gif ? (
                                <CardMedia
                                    component={record.gif.endsWith(".mp4") || record.gif.endsWith(".webm") ? "video" : "img"}
                                    autoPlay
                                    loop
                                    src={record.gif}
                                    alt="Gif not loading"
                                    style={{
                                        height: "min(70vw, 360px)",
                                        width: "auto",
                                        maxWidth: "100%",
                                        marginLeft: "auto",
                                        marginRight: "auto",
                                        lineHeight: "50px",
                                        textAlign: "center",
                                    }}
                                />
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
                                    <span>No gif found</span>
                                </Box>
                            )}
                            <CardContent>{record.smartFormattedScore ?? record.fullFormattedScore ?? "None"}</CardContent>
                        </CardActionArea>

                        <CardActions
                            style={{
                                padding: "1rem",
                                display: "flex",
                                justifyContent: "end",
                            }}
                        >
                            <Button size="small" variant="outlined" color="primary" disabled={!record.solution}>
                                <a
                                    href={record.solution}
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
                </Grid>
            ))}
        </Grid>
    )
}
