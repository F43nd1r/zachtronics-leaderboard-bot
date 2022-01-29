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

import { Dispatch, SetStateAction, useEffect, useState } from "react"

export interface Serializer<S> {
    fromString: (string: string) => S
    toString: (t: S) => string
}

type Serializable = object | string | number | boolean

export function getSerializer<S extends Serializable>(defaultValue: S) {
    const serializer: Serializer<S> =
        typeof defaultValue === "string"
            ? { fromString: (s) => s as S, toString: (s) => s as string }
            : typeof defaultValue === "number"
            ? { fromString: (s) => Number(s) as S, toString: (s) => s.toString() }
            : { fromString: (s) => JSON.parse(s), toString: (s) => JSON.stringify(s) }
    return serializer
}

export function usePersistedState<S extends Serializable>(key: string, defaultValue: S): [S, Dispatch<SetStateAction<S>>] {
    const serializer = getSerializer(defaultValue)
    const storedValue = localStorage.getItem(key)
    const [value, setValue] = useState<S>(storedValue !== null ? serializer.fromString(storedValue) : defaultValue)

    useEffect(() => {
        localStorage.setItem(key, serializer.toString(value))
    }, [value, key, serializer])

    return [value, setValue]
}
