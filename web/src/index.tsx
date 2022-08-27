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

import "./index.css"
import App from "./views/om/App"
import { lazy, StrictMode, Suspense } from "react"
import { BrowserRouter, Route, Routes } from "react-router-dom"
import LoadingIndicator from "./components/LoadingIndicator"
import { PuzzleRoutes } from "./views/om/puzzles/PuzzleView"
import FpPuzzleVisualizerView from "./views/fp/FpPuzzleVisualizerView"
import IfPuzzleVisualizerView from "./views/if/IfPuzzleVisualizerView"
import ScPuzzleVisualizerView from "./views/sc/ScPuzzleVisualizerView"
import SzPuzzleVisualizerView from "./views/sz/SzPuzzleVisualizerView"
import AppThemeProvider from "./fragments/AppThemeProvider"
import { createRoot } from "react-dom/client"

const RecentSubmissionsView = lazy(() => import("./views/om/recent/RecentSubmissionsView"))
const PuzzleView = lazy(() => import("./views/om/puzzles/PuzzleView"))
const CategoryView = lazy(() => import("./views/om/categories/CategoryView"))
const HelpView = lazy(() => import("./views/om/help/HelpView"))

createRoot(document.getElementById("root")!).render(
    <StrictMode>
        <AppThemeProvider>
            <Suspense fallback={<LoadingIndicator />}>
                <BrowserRouter>
                    <Routes>
                        <Route path="/" element={<App />}>
                            <Route path="/" element={<RecentSubmissionsView />} />
                            <Route path="puzzles/:puzzleId" element={<PuzzleView />}>
                                {PuzzleRoutes.map((route) => (
                                    <Route path={route.pathSegment} element={route.component} key={route.pathSegment} />
                                ))}
                            </Route>
                            <Route path="categories/:categoryId" element={<CategoryView />} />
                            <Route path="help" element={<HelpView />} />
                        </Route>
                        <Route path="fp/:puzzleId" element={<FpPuzzleVisualizerView />} />
                        <Route path="if/:puzzleId" element={<IfPuzzleVisualizerView />} />
                        <Route path="sc/:puzzleId" element={<ScPuzzleVisualizerView />} />
                        <Route path="sz/:puzzleId" element={<SzPuzzleVisualizerView />} />
                    </Routes>
                </BrowserRouter>
            </Suspense>
        </AppThemeProvider>
    </StrictMode>,
)
