import "./index.css"
import App from "./App"
import { lazy, StrictMode, Suspense } from "react"
import ReactDOM from "react-dom"
import { BrowserRouter, Route, Routes } from "react-router-dom"
import MainView from "./views/MainView"
import LoadingIndicator from "./components/LoadingIndicator"
import { PuzzleRoutes } from "./views/puzzles/PuzzleView"

const PuzzleView = lazy(() => import("./views/puzzles/PuzzleView"))
const CategoryView = lazy(() => import("./views/categories/CategoryView"))

ReactDOM.render(
    <StrictMode>
        <Suspense fallback={<LoadingIndicator />}>
            <BrowserRouter>
                <Routes>
                    <Route path="/" element={<App />}>
                        <Route path="/" element={<MainView />} />
                        <Route path="puzzles/:puzzleId" element={<PuzzleView />}>
                            {PuzzleRoutes.map((route) => (
                                <Route path={route.pathSegment} element={route.component} key={route.pathSegment} />
                            ))}
                        </Route>
                        <Route path="categories/:categoryId" element={<CategoryView />} />
                    </Route>
                </Routes>
            </BrowserRouter>
        </Suspense>
    </StrictMode>,
    document.getElementById("root")
)
