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
import { Static, TObject } from "@sinclair/typebox"
import { Value } from "@sinclair/typebox/value"

export function usePersistedUrlState<S extends TObject>(key: string, schema: S, defaultValue: Static<S>): [Static<S>, Dispatch<SetStateAction<Static<S>>>] {
    const serializer = getSerializer(schema, defaultValue)
    const [searchParams, setSearchParams] = useSearchParams()
    const relevantKeys = [...searchParams.keys()].filter((k) => k.startsWith(key))
    let urlValue: Static<S> | null = null
    if (relevantKeys.length) {
        urlValue = Value.Clone(defaultValue)
        for (let k of relevantKeys) {
            const path = k.split(".")
            path.shift()
            deepAssign(urlValue, path, searchParams.get(k)!)
        }
        urlValue = Value.Cast(schema, urlValue)
    }
    const storedValue = localStorage.getItem(key)
    const [value, setValue] = useState<Static<S>>(urlValue ?? (storedValue !== null ? serializer.fromString(storedValue) : defaultValue))

    useDebouncedEffect(
        (isInitialRender) => {
            if (!isInitialRender) {
                localStorage.setItem(key, serializer.toString(value))
            }

            const valueDiff = deepValues(diff(defaultValue, value)).map(({ path, value }) => ({ path: `${key}.${path}`, value }))
            const newSearchParams = new URLSearchParams(searchParams)
            let anyChanged = false
            for (let pathValue of valueDiff) {
                if (!searchParams.has(pathValue.path)) {
                    if (pathValue.value !== undefined) {
                        newSearchParams.set(pathValue.path, pathValue.value.toString())
                        anyChanged = true
                    }
                } else {
                    const oldValue = parseUrlValue(searchParams.get(pathValue.path)!)
                    if (pathValue.value !== oldValue) {
                        if (pathValue.value === undefined) {
                            newSearchParams.delete(pathValue.path)
                        } else {
                            newSearchParams.set(pathValue.path, pathValue.value.toString())
                        }
                        anyChanged = true
                    }
                }
            }
            const newKeys = valueDiff.map(({ path }) => path)
            const oldKeys = [...searchParams.keys()].filter((k) => k.startsWith(key))
            const removeKeys = oldKeys.filter((k) => !newKeys.includes(k))
            for (let removeKey of removeKeys) {
                newSearchParams.delete(removeKey)
                anyChanged = true
            }
            if (anyChanged) {
                newSearchParams.sort()
                setSearchParams(newSearchParams, { replace: true })
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
    current[lastSegment] = parseUrlValue(value)
}

function parseUrlValue(value: string): number | boolean | string {
    if (/^\d+\.?\d*$/.test(value)) {
        return parseFloat(value)
    } else if (/^(true|false)$/.test(value.toLowerCase())) {
        return value.toLowerCase() === "true"
    }
    return value
}

interface PathValue {
    path: string
    value: string | number | boolean
}

function deepValues(obj: Record<keyof any, any>): PathValue[] {
    const values: PathValue[] = []
    for (let key in obj) {
        const value = obj[key]
        if (typeof value === "object") {
            values.push(...deepValues(value).map((pathValue) => ({ path: `${key}.${pathValue.path}`, value: pathValue.value })))
        } else {
            values.push({
                path: key,
                value,
            })
        }
    }
    return values
}
