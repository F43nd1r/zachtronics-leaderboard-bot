/*
 * Copyright (c) 2026
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

import { useEffect } from "react"
import fetchFromApi from "./fetchFromApi"

export default function useDocumentTitle(title: string, resourceUrl?: string) {
    useEffect(() => {
        document.title = title

        if (!resourceUrl) {
            return
        }

        let isCurrent = true
        fetchFromApi<{ displayName: string }>(resourceUrl).then((resource) => {
            if (isCurrent) {
                document.title = `${resource.displayName} - ${title}`
            }
        })

        return () => {
            isCurrent = false
        }
    }, [title, resourceUrl])
}
