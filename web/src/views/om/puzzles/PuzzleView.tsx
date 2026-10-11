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

import Box from "@mui/material/Box"
import { Button, Tab, Tabs } from "@mui/material"
import { Link, Navigate, Outlet, useMatch } from "react-router-dom"
import { usePersistedState } from "../../../utils/usePersistedState"
import { lazy, ReactElement, useEffect } from "react"
import fetchFromApi from "../../../utils/fetchFromApi"
import Puzzle from "../../../model/Puzzle"
import LoadingIndicator from "../../../components/LoadingIndicator"
import { Static, Type } from "@sinclair/typebox"
import { castOrDefault } from "../../../utils/castOrDefault"

const PuzzleRecordsView = lazy(() => import("./records/PuzzleRecordsView"))
const PuzzleFrontierView = lazy(() => import("./frontier/PuzzleFrontierView"))
const PuzzleVisualizerView = lazy(() => import("./visualizer/OmPuzzleVisualizerView"))

const PuzzlePathSegmentSchema = Type.Union([Type.Literal("records"), Type.Literal("frontier"), Type.Literal("visualizer")])

interface RouteDefinition {
    pathSegment: Static<typeof PuzzlePathSegmentSchema>
    label: string
    component: ReactElement
}

export const PuzzleRoutes: RouteDefinition[] = [
    {
        pathSegment: "records",
        label: "Records",
        component: <PuzzleRecordsView />,
    },
    {
        pathSegment: "frontier",
        label: "Frontier",
        component: <PuzzleFrontierView />,
    },
    {
        pathSegment: "visualizer",
        label: "Visualizer",
        component: <PuzzleVisualizerView />,
    },
]

export default function PuzzleView() {
    const match = useMatch("puzzles/:puzzleId/:tab")
    const lastSegment = castOrDefault(Type.Union([PuzzlePathSegmentSchema, Type.Undefined()]), match?.params?.tab, undefined)
    const [activeTab, setActiveTab] = usePersistedState("puzzleTab", PuzzlePathSegmentSchema, PuzzleRoutes[0].pathSegment)
    const lastActiveTab = activeTab
    useEffect(() => {
        lastSegment && setActiveTab(lastSegment)
    }, [setActiveTab, lastSegment])
    const puzzleId = match?.params?.puzzleId

    useEffect(() => {
        puzzleId && fetchFromApi<Puzzle>(`/om/puzzle/${puzzleId}`).then((puzzle) => (document.title = `${puzzle.displayName} - Opus Magnum Leaderboards`))
    }, [puzzleId])

    if (!lastSegment || !PuzzleRoutes.some((route) => route.pathSegment === lastSegment)) {
        return <Navigate to={lastActiveTab} />
    }

    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: "column",
                minHeight: 0,
                flexGrow: 1,
                flexShrink: 1,
            }}
        >
            <Box
                sx={{
                    borderBottom: 1,
                    marginBottom: "1rem",
                    borderColor: "divider",
                    display: "flex",
                    justifyContent: "space-between",
                }}
            >
                {activeTab !== undefined ? (
                    <Tabs value={activeTab} onChange={(event, value) => setActiveTab(value)}>
                        {PuzzleRoutes.map((route) => (
                            <Tab label={route.label} value={route.pathSegment} to={route.pathSegment} key={route.pathSegment} component={Link} />
                        ))}
                    </Tabs>
                ) : (
                    <LoadingIndicator />
                )}
                {puzzleId?.match(/w[0-9]+/) ? (
                    <>
                        <Button
                            size="small"
                            variant="outlined"
                            color="primary"
                            style={{
                                marginTop: "auto",
                                marginBottom: "auto",
                            }}
                        >
                            <a
                                href={`https://steamcommunity.com/sharedfiles/filedetails/?id=${puzzleId.substring(1)}`}
                                target="_blank"
                                rel="noreferrer"
                                style={{
                                    color: "inherit",
                                    textDecoration: "none",
                                }}
                            >
                                Download Puzzle
                            </a>
                        </Button>
                        <Button
                            size="small"
                            variant="outlined"
                            color="primary"
                            style={{
                                marginTop: "auto",
                                marginBottom: "auto",
                            }}
                        >
                            <a
                                href={`https://steamcommunity.com/sharedfiles/filedetails/?id=${puzzleId.substring(1)}`}
                                target="_blank"
                                rel="noreferrer"
                                style={{
                                    color: "inherit",
                                    textDecoration: "none",
                                }}
                            >
                                Get on Steam
                            </a>
                        </Button>
                    </>
                ) : (
                    <Button
                        size="small"
                        variant="outlined"
                        color="primary"
                        disabled={true}
                        style={{
                            marginTop: "auto",
                            marginBottom: "auto",
                        }}
                    >
                        Built in puzzle
                    </Button>
                )}
            </Box>
            <Outlet />
        </Box>
    )
}
