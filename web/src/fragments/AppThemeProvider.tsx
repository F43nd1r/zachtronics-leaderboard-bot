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
import { createContext, ReactNode, useMemo } from "react"
import { createTheme, ThemeProvider } from "@mui/material/styles"
import CssBaseline from "@mui/material/CssBaseline"

export const AppThemeContext = createContext({
    toggleColorMode: () => {},
})

export default function AppThemeProvider(props: { children?: ReactNode | undefined }) {
    const prefersDarkMode = useMediaQuery("(prefers-color-scheme: dark)")
    const [mode, setMode] = usePersistedState("colorMode", prefersDarkMode ? "light" : "dark")
    const colorMode = useMemo(
        () => ({
            toggleColorMode: () => {
                setMode((prevMode) => (prevMode === "light" ? "dark" : "light"))
            },
        }),
        [setMode],
    )

    const theme = useMemo(
        () =>
            createTheme({
                palette: {
                    mode,
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
        [mode],
    )

    return (
        <AppThemeContext.Provider value={colorMode}>
            <ThemeProvider theme={theme}>
                <CssBaseline />
                {props.children}
            </ThemeProvider>
        </AppThemeContext.Provider>
    )
}

declare module "@mui/material/styles" {
    interface BreakpointOverrides {
        xxl: true
        xxxl: true
    }
}
