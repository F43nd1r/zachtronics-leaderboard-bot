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
import useMediaQuery from "@mui/material/useMediaQuery"
import { usePersistedState } from "../utils/usePersistedState"
import { createContext, ReactNode, useContext, useMemo } from "react"
import { createTheme, ThemeProvider } from "@mui/material/styles"
import CssBaseline from "@mui/material/CssBaseline"

interface AppSettings {
    colorMode: "light" | "dark"
    setColorMode: (value: "light" | "dark") => void
    autoPlay: boolean
    setAutoPlay: (value: boolean) => void
    showControls: boolean
    setShowControls: (value: boolean) => void
}

export const AppSettingsContext = createContext<AppSettings>({
    colorMode: "dark",
    setColorMode: () => {},
    autoPlay: true,
    setAutoPlay: () => {},
    showControls: false,
    setShowControls: () => {},
})

export default function AppSettingsProvider(props: { children?: ReactNode | undefined }) {
    const prefersDarkMode = useMediaQuery("(prefers-color-scheme: dark)")
    const [colorMode, setColorMode] = usePersistedState("colorMode", prefersDarkMode ? "light" : "dark")
    const [autoPlay, setAutoPlay] = usePersistedState<boolean>("autoPlay", true)
    const [showControls, setShowControls] = usePersistedState<boolean>("showControls", false)
    const context = useMemo<AppSettings>(
        () => ({
            colorMode,
            setColorMode,
            autoPlay,
            setAutoPlay,
            showControls,
            setShowControls,
        }),
        [autoPlay, colorMode, setAutoPlay, setColorMode, setShowControls, showControls],
    )

    const theme = useMemo(
        () =>
            createTheme({
                palette: {
                    mode: colorMode,
                },
                breakpoints: {
                    values: {
                        xs: 0,
                        sm: 640,
                        md: 960,
                        lg: 1280,
                        xl: 1600,
                        xxl: 1920,
                        xxxl: 2240,
                    },
                },
            }),
        [colorMode],
    )

    return (
        <AppSettingsContext.Provider value={context}>
            <ThemeProvider theme={theme}>
                <CssBaseline />
                {props.children}
            </ThemeProvider>
        </AppSettingsContext.Provider>
    )
}

export const useAppSettings = () => {
    const appSettings = useContext(AppSettingsContext)
    if (appSettings == null) throw Error("App Settings context required")
    return appSettings
}

declare module "@mui/material/styles" {
    interface BreakpointOverrides {
        xxl: true
        xxxl: true
    }
}
