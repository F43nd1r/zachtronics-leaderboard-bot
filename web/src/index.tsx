import "./index.css"
import App from "./App"
import React, { lazy, Suspense } from "react"
import ReactDOM from "react-dom"
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"
import MainView from "./views/MainView"
import LoadingIndicator from "./components/LoadingIndicator"

const PuzzleView = lazy(() => import("./views/PuzzleView"))
const PuzzleRecordsView = lazy(() => import("./views/PuzzleRecordsView"))
const PuzzleFrontierView = lazy(() => import("./views/PuzzleFrontierView"))
const CategoryView = lazy(() => import("./views/CategoryView"))

ReactDOM.render(
    <React.StrictMode>
        <Suspense fallback={<LoadingIndicator />}>
            <BrowserRouter>
                <Routes>
                    <Route path="/" element={<App />}>
                        <Route path="/" element={<MainView />} />
                        <Route path="puzzles/:puzzleId" element={<PuzzleView />}>
                            <Route path="records" element={<PuzzleRecordsView />} />
                            <Route path="frontier" element={<PuzzleFrontierView />} />
                            <Route path="" element={<Navigate to="records" />} />
                        </Route>
                        <Route path="categories/:categoryId" element={<CategoryView />} />
                    </Route>
                </Routes>
            </BrowserRouter>
        </Suspense>
    </React.StrictMode>,
    document.getElementById("root")
)
