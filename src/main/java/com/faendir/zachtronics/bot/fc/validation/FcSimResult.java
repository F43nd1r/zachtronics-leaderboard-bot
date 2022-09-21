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

package com.faendir.zachtronics.bot.fc.validation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

/**
 <pre>{
 "level_number": 14,
 "level_name": "The Commissary",
 "level_slug": "the-commissary",
 "solution_name": "New Solution 1",
 "filename": "the-commissary-1.solution", // null for stdin
 "marked_solved": true,
 "is_correct": true,
 "cost": 186,
 "max_time": 14,
 "total_time": 68,
 "num_wires": 47
 "solution": "9QMAA..."
 }</pre> or
 <pre>{
 "level_number": 21,
 "level_name": "Sushi Yeah!",
 "level_slug": "sushi-yeah!",
 "solution_name": "New Solution 1 (Copy) (Copy)",
 "filename": "sushi-yeah!-3.solution",
 "marked_solved": true,
 "is_correct": false,
 "error_type": "EmergencyStop",
 "error_message": "Order 1, tick 4 @ (5, 4), (5, 5): Emergency stop: These products cannot be stacked."
 }</pre> or
 <pre>{
 "is_correct": false,
 "error_type": "InvalidSolutionError",
 "error_message": "invalid solution version 67324752 (must be between 1000 and 1013)",
 "filename": "stdin"
 }</pre>
 */
@Value
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FcSimResult {
    /** present iff the solution was parseable */
    @Nullable Integer levelNumber;
    @Nullable String levelName;
    @Nullable String levelSlug;
    @Nullable String solutionName;

    /** <tt>null</tt> for stdin */
    @Nullable String filename;
    boolean markedSolved;
    boolean isCorrect;

    /** nonzero iff the solution was valid */
    int cost;
    int maxTime;
    int totalTime;
    int numWires;

    /** present iff <tt>--include-solution</tt> is given, base64 encoded */
    @Nullable String solution;

    /** present iff the solution is invalid or unparseable */
    @Nullable String errorType;
    /** present iff the solution is invalid or unparseable */
    @Nullable String errorMessage;
}
