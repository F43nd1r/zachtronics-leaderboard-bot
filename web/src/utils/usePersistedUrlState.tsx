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

import { Dispatch, SetStateAction, useState } from "react"
import { useSearchParams } from "react-router-dom"
import { useDebouncedEffect } from "./useDebouncedEffect"
import { diff } from "deep-object-diff"
import { getSerializer } from "./usePersistedState"

export function usePersistedUrlState<S extends object>(key: string, defaultValue: S): [S, Dispatch<SetStateAction<S>>] {
    const serializer = getSerializer(defaultValue)
    const [searchParams, setSearchParams] = useSearchParams()
    const relevantKeys = [...searchParams.keys()].filter((k) => k.startsWith(key))
    let urlValue: S | null = null
    if (relevantKeys.length) {
        urlValue = { ...defaultValue }
        for (let k of relevantKeys) {
            const path = k.split(".")
            path.shift()
            deepAssign(urlValue, path, searchParams.get(k)!)
        }
    }
    const storedValue = localStorage.getItem(key)
    const [value, setValue] = useState<S>(urlValue ?? (storedValue !== null ? serializer.fromString(storedValue) : defaultValue))

    useDebouncedEffect(
        (isInitialRender) => {
            if (!isInitialRender) {
                localStorage.setItem(key, serializer.toString(value))
            }

            const valueDiff = deepValues(diff(defaultValue, value), key).map(({ path, value }) => ({ path: `${key}.${path}`, value }))
            let anyChanged = false
            for (let pathValue of valueDiff) {
                // eslint-disable-next-line eqeqeq
                if (!searchParams.has(pathValue.path) || pathValue.value != searchParams.get(pathValue.path)) {
                    if (pathValue.value === undefined) {
                        searchParams.delete(pathValue.path)
                    } else {
                        searchParams.set(pathValue.path, pathValue.value)
                    }
                    anyChanged = true
                }
            }
            const newKeys = valueDiff.map(({ path }) => path)
            const oldKeys = [...searchParams.keys()].filter((k) => k.startsWith(key))
            const removeKeys = oldKeys.filter((k) => !newKeys.includes(k))
            for (let removeKey of removeKeys) {
                searchParams.delete(removeKey)
                anyChanged = true
            }
            if (anyChanged) {
                setSearchParams(searchParams)
            }
        },
        [value, key, serializer, searchParams, setSearchParams, defaultValue],
        500,
    )

    return [value, setValue]
}

function deepAssign(obj: Record<keyof any, any>, path: string[], value: string) {
    if (!path.length) return
    let current: Record<keyof any, any> = obj
    const lastSegment = path.pop()!
    for (let segment of path) {
        if (current[segment] === undefined) {
            current[segment] = {}
        }
        current = current[segment]
    }
    let parsedValue: any = value
    if (/\d+\.?\d*/.test(value)) {
        parsedValue = parseFloat(value)
    } else if (/(true|false)/.test(value.toLowerCase())) {
        parsedValue = !!value
    }
    current[lastSegment] = parsedValue
}

interface PathValue {
    path: string
    value: any
}

function deepValues(obj: Record<keyof any, any>, ignoreKey: string): PathValue[] {
    const values: PathValue[] = []
    for (let key in obj) {
        if (key === ignoreKey) continue
        const value = obj[key]
        if (typeof value === "object") {
            values.push(...deepValues(value, ignoreKey).map((pathValue) => ({ path: `${key}.${pathValue.path}`, value: pathValue.value })))
        } else {
            values.push({
                path: key,
                value,
            })
        }
    }
    return values
}
