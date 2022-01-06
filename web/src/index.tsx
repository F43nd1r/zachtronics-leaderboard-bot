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
import App from "./App"
import { lazy, StrictMode, Suspense } from "react"
import ReactDOM from "react-dom"
import { BrowserRouter, Route, Routes } from "react-router-dom"
import LoadingIndicator from "./components/LoadingIndicator"
import { PuzzleRoutes } from "./views/puzzles/PuzzleView"

const RecentSubmissionsView = lazy(() => import("./views/recent/RecentSubmissionsView"))
const PuzzleView = lazy(() => import("./views/puzzles/PuzzleView"))
const CategoryView = lazy(() => import("./views/categories/CategoryView"))
const HelpView = lazy(() => import("./views/help/HelpView"))

ReactDOM.render(
    <StrictMode>
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
                </Routes>
            </BrowserRouter>
        </Suspense>
    </StrictMode>,
    document.getElementById("root"),
)
