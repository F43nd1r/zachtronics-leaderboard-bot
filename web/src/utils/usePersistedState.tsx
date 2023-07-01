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
import { Static, TSchema, TypeGuard } from "@sinclair/typebox"
import { castOrDefault } from "./castOrDefault"

export interface Serializer<S> {
    fromString: (string: string) => S
    toString: (t: S) => string
}

function isStringSchema(schema: TSchema): boolean {
    return TypeGuard.TString(schema) || TypeGuard.TLiteralString(schema) || (TypeGuard.TUnion(schema) && schema.anyOf.every(isStringSchema))
}

function isNumberSchema(schema: TSchema): boolean {
    return TypeGuard.TNumber(schema) || TypeGuard.TLiteralNumber(schema) || (TypeGuard.TUnion(schema) && schema.anyOf.every(isNumberSchema))
}

export function getSerializer<S extends TSchema>(schema: S, defaultValue: Static<S>): Serializer<Static<S>> {
    if (isStringSchema(schema)) {
        return { fromString: (s) => castOrDefault(schema, s, defaultValue), toString: (s) => s as string }
    } else if (isNumberSchema(schema)) {
        return { fromString: (s) => castOrDefault(schema, Number(s), defaultValue), toString: (s) => s!.toString() }
    } else {
        return { fromString: (s) => castOrDefault(schema, JSON.parse(s), defaultValue), toString: (s) => JSON.stringify(s) }
    }
}

export function usePersistedState<S extends TSchema>(key: string, schema: S, defaultValue: Static<S>): [Static<S>, Dispatch<SetStateAction<Static<S>>>] {
    const serializer = getSerializer(schema, defaultValue)
    const storedValue = localStorage.getItem(key)
    const [value, setValue] = useState<Static<S>>(storedValue !== null ? serializer.fromString(storedValue) : defaultValue)

    useEffect(() => {
        localStorage.setItem(key, serializer.toString(value))
    }, [value, key, serializer])

    return [value, setValue]
}
