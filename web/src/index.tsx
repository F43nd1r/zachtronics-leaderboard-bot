import "./index.css"
import App from "./App"
import { lazy, StrictMode, Suspense } from "react"
import ReactDOM from "react-dom"
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"
import MainView from "./views/MainView"
import LoadingIndicator from "./components/LoadingIndicator"

const PuzzleView = lazy(() => import("./views/puzzles/PuzzleView"))
const PuzzleRecordsView = lazy(() => import("./views/puzzles/records/PuzzleRecordsView"))
const PuzzleFrontierView = lazy(() => import("./views/puzzles/frontier/PuzzleFrontierView"))
const PuzzleVisualizerView = lazy(() => import("./views/puzzles/visualizer/PuzzleVisualizerView"))
const CategoryView = lazy(() => import("./views/categories/CategoryView"))

ReactDOM.render(
    <StrictMode>
        <Suspense fallback={<LoadingIndicator />}>
            <BrowserRouter>
                <Routes>
                    <Route path="/" element={<App />}>
                        <Route path="/" element={<MainView />} />
                        <Route path="puzzles/:puzzleId" element={<PuzzleView />}>
                            <Route path="records" element={<PuzzleRecordsView />} />
                            <Route path="frontier" element={<PuzzleFrontierView />} />
                            <Route path="visualizer" element={<PuzzleVisualizerView />} />
                            <Route path="" element={<Navigate to="records" />} />
                        </Route>
                        <Route path="categories/:categoryId" element={<CategoryView />} />
                    </Route>
                </Routes>
            </BrowserRouter>
        </Suspense>
    </StrictMode>,
    document.getElementById("root")
)
