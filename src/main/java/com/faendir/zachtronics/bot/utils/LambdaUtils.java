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

package com.faendir.zachtronics.bot.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Function;

public final class LambdaUtils {
    private LambdaUtils() {}

    public interface FunctionIOException<T, R> {
        R apply(T t) throws IOException;
    }

    @NotNull
    public static <T, R> Function<T, R> uncheckIOException(FunctionIOException<T, R> excFunction) {
        return t -> {
            try {
                return excFunction.apply(t);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}
