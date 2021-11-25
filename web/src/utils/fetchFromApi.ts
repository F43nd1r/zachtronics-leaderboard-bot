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

import { ComponentState } from "./ComponentState"

export default function fetchFromApi<T>(url: string, setState: (state: ComponentState) => void): Promise<T> {
    return fetch(`${window.location.protocol}//${window.location.host}/om${url}`)
        .then((response) => {
            if (response.ok) {
                return response.json()
            } else {
                return Promise.reject(response.status)
            }
        })
        .then(
            (data) => {
                setState(ComponentState.READY)
                return data
            },
            () => {
                setState(ComponentState.ERROR)
            }
        )
}
