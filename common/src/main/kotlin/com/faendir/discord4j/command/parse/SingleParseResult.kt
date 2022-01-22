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

package com.faendir.discord4j.command.parse


sealed class SingleParseResult<T> {
    class Success<T>(val value: T) : SingleParseResult<T>()
    class Ambiguous<T>(val options: List<T>, val stringify: (T) -> String) : SingleParseResult<T>()
    class Failure<T>(val message: String) : SingleParseResult<T>()

    /**
     * If it's [Success], returns the value, otherwise returns
     * [other]
     *
     * @param [other] the value to be returned, if it's not a [Success]
     * @return the value, if present, otherwise [other]
     */
    fun orElse(other: T): T {
        return if (this is Success) value else other
    }

    /**
     * If it's [Success], returns the value, otherwise throws
     * [IllegalArgumentException]
     *
     * @return the value contained in this [Success]
     * @throws [IllegalArgumentException] if it's not a [Success]
     */
    fun orElseThrow(): T {
        when (this) {
            is Success -> return value
            is Ambiguous -> throw IllegalArgumentException("your request was not precise enough. Use one of:\n" +
                    options.joinToString("\n") { "- *${stringify(it)}*" })
            is Failure -> throw IllegalArgumentException(message)
        }
    }
}

