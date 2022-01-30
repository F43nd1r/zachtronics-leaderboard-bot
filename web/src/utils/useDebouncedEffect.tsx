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

import { DependencyList, useEffect, useRef } from "react"

export function useDebouncedEffect(effect: (isInitialRender: boolean) => void, deps: DependencyList, wait: number): void {
    const isInitialRender = useRef(true)
    useEffect(() => {
        if (isInitialRender.current) {
            isInitialRender.current = false
            effect(true)
        } else {
            const id = setTimeout(() => effect(false), wait)
            return () => clearTimeout(id)
        }
        //eslint-disable-next-line react-hooks/exhaustive-deps
    }, deps)
}
