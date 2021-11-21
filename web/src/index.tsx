import "./index.css"
import App from "./App"
import React from "react"
import ReactDOM from "react-dom"
import { BrowserRouter, Route, Routes } from "react-router-dom"
import PuzzleView from "./views/PuzzleView"
import CategoryView from "./views/CategoryView"
import MainView from "./views/MainView"

ReactDOM.render(
    <React.StrictMode>
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<App />}>
                    <Route path="/" element={<MainView />} />
                    <Route path="puzzles/:puzzleId" element={<PuzzleView />} />
                    <Route path="categories/:categoryId" element={<CategoryView />} />
                </Route>
            </Routes>
        </BrowserRouter>
    </React.StrictMode>,
    document.getElementById("root")
)
