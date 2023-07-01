/*
 * Copyright (c) 2021
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
import LoadingIndicator from "../components/LoadingIndicator"
import { Error } from "@mui/icons-material"
import { Box } from "@mui/material"
import { ReactNode, useEffect, useState } from "react"
import { ComponentState } from "./ComponentState"
import fetchFromApi from "./fetchFromApi"

interface ApiResourceProps<T> {
    url: string
    element: (t: T) => ReactNode
}

export default function ApiResource<T>(props: ApiResourceProps<T>): ReactNode {
    const [state, setState] = useState(ComponentState.LOADING)
    const [data, setData] = useState<T | undefined>(undefined)
    useEffect(() => {
        fetchFromApi<T>(props.url).then(
            (data) => {
                setData(data)
                setState(ComponentState.READY)
            },
            () => {
                setState(ComponentState.ERROR)
            },
        )
    }, [props.url])

    switch (state) {
        case ComponentState.LOADING:
            return <LoadingIndicator />
        case ComponentState.ERROR:
            return (
                <Box
                    sx={{
                        display: "flex",
                        flexDirection: "column",
                        margin: "auto",
                        alignItems: "center",
                    }}
                >
                    <Error fontSize="large" sx={{ marginBottom: "1rem" }} />
                    <span>Failed to load data</span>
                </Box>
            )
        case ComponentState.READY:
            return props.element(data as T)
    }
}
