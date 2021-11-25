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
import { Tab, Tabs } from "@mui/material"
import { Link, Navigate, Outlet, useMatch } from "react-router-dom"
import { usePersistedStringState } from "../../utils/usePersistedState"
import { lazy, ReactElement, useEffect } from "react"

const PuzzleRecordsView = lazy(() => import("./records/PuzzleRecordsView"))
const PuzzleFrontierView = lazy(() => import("./frontier/PuzzleFrontierView"))
const PuzzleVisualizerView = lazy(() => import("./visualizer/PuzzleVisualizerView"))

interface RouteDefinition {
    pathSegment: string
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
    const lastSegment = match?.params?.tab
    const [activeTab, setActiveTab] = usePersistedStringState<string>("puzzleTab", PuzzleRoutes[0].pathSegment)
    const lastActiveTab = activeTab
    useEffect(() => {
        lastSegment && setActiveTab(lastSegment)
    }, [setActiveTab, lastSegment])

    if (!lastSegment || !PuzzleRoutes.some((route) => route.pathSegment === lastSegment)) {
        return <Navigate to={lastActiveTab} />
    }

    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: "column",
                flexGrow: 1,
            }}
        >
            <Box
                sx={{
                    borderBottom: 1,
                    marginBottom: "1rem",
                    borderColor: "divider",
                }}
            >
                <Tabs value={activeTab} onChange={(event, value) => setActiveTab(value)}>
                    {PuzzleRoutes.map((route) => (
                        <Tab label={route.label} value={route.pathSegment} to={route.pathSegment} key={route.pathSegment} component={Link} />
                    ))}
                </Tabs>
            </Box>
            <Outlet />
        </Box>
    )
}
