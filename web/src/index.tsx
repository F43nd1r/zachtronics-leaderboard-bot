import "./index.css"
import App from "./App"
import React from "react"
import ReactDOM from "react-dom"
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"
import PuzzleRecordsView from "./views/PuzzleRecordsView"
import CategoryView from "./views/CategoryView"
import MainView from "./views/MainView"
import PuzzleView from "./views/PuzzleView"
import PuzzleFrontierView from "./views/PuzzleFrontierView"

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
