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
import { Link, Outlet, useLocation } from "react-router-dom"

export default function PuzzleView() {
    const location = useLocation()
    const lastSegment = location.pathname.split("/").reverse()[0]

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
                <Tabs value={lastSegment}>
                    <Tab label="Records" value="records" to="records" component={Link} />
                    <Tab label="Frontier" value="frontier" to="frontier" component={Link} />
                    <Tab label="Visualizer" value="visualizer" to="visualizer" component={Link} />
                </Tabs>
            </Box>
            <Outlet />
        </Box>
    )
}
