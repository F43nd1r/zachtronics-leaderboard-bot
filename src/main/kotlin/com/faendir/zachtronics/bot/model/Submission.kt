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

package com.faendir.zachtronics.bot.model

/** Write interface, used to store data from the user that needs to be stored */
interface Submission<C : Category, P: Puzzle> {
    val puzzle: P
    val score: Score<C>
    val author: String?
    /** Human-readable solution representation */
    val displayLink: String?
    /** Machine-readable solution representation */
    val data: Any
}