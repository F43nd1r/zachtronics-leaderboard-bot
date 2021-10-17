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

sealed class CombinedParseResult<T> {
    class Success<T>(val value: T) : CombinedParseResult<T>()

    class Ambiguous<T>(val options: Map<String, SingleParseResult.Ambiguous<*>>, val partialResult: Map<String, Any?>) : CombinedParseResult<T>() {
        @Suppress("UNCHECKED_CAST")
        fun <U> typed() =  this as Ambiguous<U>
    }
    class Failure<T>(val messages: List<String>) : CombinedParseResult<T>() {
        @Suppress("UNCHECKED_CAST")
        fun <U> typed() =  this as Failure<U>
    }

    companion object {
        fun <T> combine(results: Map<String, SingleParseResult<*>>, constructor: (Map<String, Any?>) -> T): CombinedParseResult<T> {
            results.values.filterIsInstance<SingleParseResult.Failure<*>>().takeIf { it.isNotEmpty() }?.let { failures ->
                return Failure(failures.map { it.message })
            }
            val success = results.filterValues { it is SingleParseResult.Success<*> }.mapValues { (it.value as SingleParseResult.Success<*>).value }
            results.filterValues { it is SingleParseResult.Ambiguous<*> }.takeIf { it.isNotEmpty() }?.let { ambiguous ->
                return Ambiguous(ambiguous.mapValues { (_, result) -> (result as SingleParseResult.Ambiguous<*>) }, success)
            }
            return Success(constructor(success))
        }
    }
}