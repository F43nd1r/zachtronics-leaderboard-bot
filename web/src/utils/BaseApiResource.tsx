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
import { useEffect, useState } from "react"
import { ComponentState } from "./ComponentState"

interface BaseApiResourceProps<T, R> {
    url: string
    loading: R
    error: R
    element: (t: T) => R
}

export default function BaseApiResource<T, R>(props: BaseApiResourceProps<T, R>): R {
    const [state, setState] = useState(ComponentState.LOADING)
    const [data, setData] = useState<T | undefined>(undefined)
    useEffect(() => {
        fetch(`${window.location.protocol}//${window.location.host}/om${props.url}`)
            .then((response) => {
                if (response.ok) {
                    return response.json()
                } else {
                    return Promise.reject(response.status)
                }
            })
            .then(
                (data) => {
                    setData(data)
                    setState(ComponentState.READY)
                },
                () => {
                    setState(ComponentState.ERROR)
                }
            )
    }, [props.url])

    switch (state) {
        case ComponentState.LOADING:
            return props.loading
        case ComponentState.ERROR:
            return props.error
        case ComponentState.READY:
            return props.element(data as T)
    }
}
