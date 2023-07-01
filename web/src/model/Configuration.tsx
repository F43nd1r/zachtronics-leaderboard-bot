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

import { Static, Type } from "@sinclair/typebox"

export type Mode = "2D" | "3D"

export const MetricConfigurationSchema = Type.Object({
    metric: Type.String(),
    scale: Type.Union([Type.Literal("linear"), Type.Literal("log")]),
})

export type MetricConfiguration = Static<typeof MetricConfigurationSchema>

export const ConfigurationSchema = Type.Object({
    mode: Type.Union([Type.Literal("2D"), Type.Literal("3D")]),
    x: MetricConfigurationSchema,
    y: MetricConfigurationSchema,
    z: MetricConfigurationSchema,
})

export type Configuration = Static<typeof ConfigurationSchema>
