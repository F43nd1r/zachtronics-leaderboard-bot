/*
 * Copyright (c) 2025
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

package com.faendir.zachtronics.bot.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

interface Score<C : Category> {
    fun toDisplayString() = toDisplayString(DisplayContext.plainText())

    fun toDisplayString(context: DisplayContext<C>): String
}

class ScorePlainSerializer : JsonSerializer<Score<*>>() {
    override fun serialize(value: Score<*>, gen: JsonGenerator, serializers: SerializerProvider?) {
        gen.writeString(value.toDisplayString())
    }
}
