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
import org.springframework.boot.gradle.tasks.bundling.BootJar

@Suppress("DSL_SCOPE_VIOLATION") // TODO remove when https://youtrack.jetbrains.com/issue/KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
    alias(libs.plugins.gradle.docker)
}

docker {
    name = "f43nd1r/opus-magnum-gif-maker"
    files(tasks.getByName<BootJar>("bootJar").outputs)
    copySpec.into("build/libs")
}

dependencies {
    implementation(libs.spring.boot.web)
    implementation(libs.java.ffmpeg)
    implementation(libs.kotlinx.json)
    implementation(libs.jackson.kotlin)

    testImplementation(libs.spring.boot.test)
    testImplementation(libs.springmockk)
    testImplementation(libs.strikt)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}