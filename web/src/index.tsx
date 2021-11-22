import "./index.css"
import App from "./App"
import React, { lazy } from "react"
import ReactDOM from "react-dom"
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"
import MainView from "./views/MainView"

const PuzzleView = lazy(() => import("./views/PuzzleView"))
const PuzzleRecordsView = lazy(() => import("./views/PuzzleRecordsView"))
const PuzzleFrontierView = lazy(() => import("./views/PuzzleFrontierView"))
const CategoryView = lazy(() => import("./views/CategoryView"))

ReactDOM.render(
    <React.StrictMode>
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
    </React.StrictMode>,
    document.getElementById("root")
)
