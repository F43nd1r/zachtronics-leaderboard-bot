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

@Suppress("DSL_SCOPE_VIOLATION") // TODO remove when https://youtrack.jetbrains.com/issue/KTIJ-19369 is fixed
plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.nokee.jni)
    alias(libs.plugins.nokee.c.language)
    alias(libs.plugins.gradle.lombok)
}

group = ""

library {
    targetMachines.set(listOf(machines.linux.x86_64))

    dependencies {
        nativeImplementation(projects.omsim)
    }
}

dependencies {
    testImplementation(libs.junit)
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
    withType<CCompile> {
        isOptimized = true
        compilerArgs.addAll(
            listOf(
                "-Wall",
                "-Wextra",
                "-Wfloat-equal",
                "-Wundef",
                "-Wshadow",
                "-Wpointer-arith",
                "-Wcast-align",
                "-Wstrict-prototypes",
                "-Wstrict-overflow=5",
                "-Wwrite-strings",
                "-Waggregate-return",
                "-Wcast-qual",
                "-Wswitch-default",
                "-Wswitch-enum",
                "-Wconversion",
                "-Wunreachable-code",
                "-Wmissing-declarations",
                "-Werror",
                "-std=c11"
            )
        )
    }
}
