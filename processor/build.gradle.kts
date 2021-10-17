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

plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:${properties["kspVersion"]}")
    implementation(project(":common"))
    implementation("com.squareup:kotlinpoet:1.10.1")
    implementation("com.faendir:kotlinbard:0.4.0")
    implementation("com.discord4j:discord4j-core:${properties["discord4jVersion"]}")
    implementation("net.pearx.kasechange:kasechange-jvm:1.3.0")
    implementation(kotlin("compiler-embeddable"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.5")
    testImplementation("io.strikt:strikt-core:0.31.0")
    testImplementation(kotlin("scripting-compiler-embeddable"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}