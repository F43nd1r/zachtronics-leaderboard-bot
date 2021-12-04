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
    alias(libs.plugins.nokee.c.library)
}

library {
    targetMachines.set(listOf(machines.linux.x86_64))

    val files = file("src/main/c/Makefile").readLines().first { it.startsWith("libverify.so: ") }.removePrefix("libverify.so: ").split(" ")

    sources.c.configure {
        filter {
            include(files.filter { it.endsWith(".c") }.map { "**/$it" })
        }
    }
    sources.public.configure {
        from("${project.projectDir}/src/main/c")
        filter {
            include(files.filter { it.endsWith(".h") }.map { "**/$it" })
        }
    }
}

tasks {
    withType<CCompile> {
        isOptimized = true
    }
}