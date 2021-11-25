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

package com.faendir.zachtronics.bot.validation;


import com.faendir.zachtronics.bot.model.Submission;
import lombok.Value;

public sealed interface ValidationResult<S extends Submission<?, ?>> permits ValidationResult.Valid,
                                                                             ValidationResult.Invalid,
                                                                             ValidationResult.Unparseable {
    S getSubmission();
    String getMessage();

    @Value
    final class Valid<S extends Submission<?, ?>> implements ValidationResult<S> {
        S submission;

        @Override
        public String getMessage() {
            throw new IllegalArgumentException();
        }
    }

    @Value
    final class Invalid<S extends Submission<?, ?>> implements ValidationResult<S> {
        S submission;
        String message;
    }

    @Value
    final class Unparseable<S extends Submission<?, ?>> implements ValidationResult<S> {
        String message;

        @Override
        public S getSubmission() {
            throw new IllegalArgumentException(message);
        }
    }
}


