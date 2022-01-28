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

import { Helmet } from "react-helmet"

export interface PageMetaProps {
    title: string
    siteName?: string
    description?: string
    image?: string
}

export default function PageMeta({ description, image, title, siteName = "Opus Magnum Leaderboards" }: PageMetaProps) {
    return (
        <Helmet>
            <title>{`${title} - ${siteName}`}</title>
            <meta property="og:title" content={title} />
            <meta property="og:site_name" content={siteName} />
            {description && <meta name="description" content={description} />}
            {description && <meta property="og:description" content={description} />}
            {image && <meta property="og:image" content={image} />}
            {image && <meta name="twitter:card" content="summary_large_image" />}
        </Helmet>
    )
}
