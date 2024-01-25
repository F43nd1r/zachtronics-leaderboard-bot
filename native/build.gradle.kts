/*
 * Copyright (c) 2023
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
plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.nokee.jni)
    alias(libs.plugins.nokee.c.language)
    alias(libs.plugins.gradle.lombok)
}

group = ""

sourceSets {
    create("omsim")
}

library {
    targetMachines.set(listOf(machines.linux.x86_64))

    val grabFiles = { type: String -> // this is megabrittle
        file("src/omsim/c/Makefile").readLines().first { it.startsWith("$type=") }.removePrefix("$type=").split(" ")
            .map { file("src/omsim/c/$it") }
    }

    cSources.from(grabFiles("SOURCE"))
    privateHeaders.from(grabFiles("HEADER"))
}

dependencies {
    testImplementation(libs.junit)
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.WARN // TODO
    }
    withType<CCompile> {
        isOptimized = true
    }
}
