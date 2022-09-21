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

package com.faendir.zachtronics.bot.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ValidationUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(
            DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    private ValidationUtils() {}

    public static <T> T callValidator(Class<T> resultClass, byte[] data, String... command) {
        ProcessBuilder builder = new ProcessBuilder(command);

        try {
            Process process = builder.start();
            process.getOutputStream().write(data);
            process.getOutputStream().close();

            byte[] result = process.getInputStream().readAllBytes();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                byte[] stderr = process.getErrorStream().readAllBytes();
                if (stderr.length != 0)
                    throw new ValidationException(new String(stderr));
            }
            return OBJECT_MAPPER.readValue(result, resultClass);

        } catch (JsonProcessingException e) {
            throw new ValidationException("Error in reading back results", e);
        } catch (IOException e) {
            throw new ValidationException("Error in communicating with the validator", e);
        } catch (InterruptedException e) {
            throw new ValidationException("Thread was killed while waiting for the validator", e);
        }
    }
}
