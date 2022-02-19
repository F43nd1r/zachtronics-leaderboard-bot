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

package com.faendir.zachtronics.bot.rest.dto

import com.faendir.zachtronics.bot.repository.SubmitResult
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

enum class SubmitResultType {
    ALREADY_PRESENT,
    NOTHING_BEATEN,
    SUCCESS
}

fun SubmitResult<*, *>.toType(): SubmitResultType {
    return when (this) {
        is SubmitResult.Success -> SubmitResultType.SUCCESS
        is SubmitResult.AlreadyPresent -> SubmitResultType.ALREADY_PRESENT
        is SubmitResult.NothingBeaten -> SubmitResultType.NOTHING_BEATEN
        is SubmitResult.Failure -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, this.message)
    }
}