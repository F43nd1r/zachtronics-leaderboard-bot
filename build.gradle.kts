/*
 * Copyright (c) 2025
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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.kotlin.plugin.lombok)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
    alias(libs.plugins.gradle.lombok)
    alias(libs.plugins.gradle.gitProperties)
    alias(libs.plugins.gradle.frontend)
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
        mavenLocal()
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
            freeCompilerArgs = listOf("-Xjvm-default=all", "-Xjsr305=strict")
        }
    }
}

dependencies {
    implementation(libs.kotlinx.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.spring.boot.web)
    implementation(libs.mysqlConnector)
    implementation(libs.spring.cloud)
    implementation(libs.discord4j)
    implementation(libs.reactor.kotlin)
    implementation(libs.jgit)
    implementation(libs.java.curl)
    implementation(libs.jraw)
    implementation(libs.om.dsl)
    implementation(libs.om.parser)
    implementation(projects.native)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.jsr310)
    implementation(libs.springdoc)
    implementation(libs.guava)
    implementation(libs.opencsv)
    implementation(libs.ffmpeg)

    testImplementation(libs.spring.boot.test)
    testImplementation(libs.trove4j)
    testImplementation(libs.springmockk)
    testImplementation(libs.strikt)
}

tasks.withType<Test> {
    useJUnitPlatform()
    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}

        override fun beforeTest(testDescriptor: TestDescriptor) {}

        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}

        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent == null) {
                println(
                    "Test results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} " +
                            "failures, ${result.skippedTestCount} skipped)"
                )
            }
        }
    })
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

kotlinLombok {
    lombokConfigurationFile(file("lombok.config"))
}

frontend {
    nodeVersion.set(libs.versions.nodejs)
    nodeInstallDirectory.set(file("${project.buildDir}/nodejs"))
    packageJsonDirectory.set(file("web"))
    assembleScript.set("build")
    cleanScript.set("clean")
}

tasks.installNode {
    inputs.property("nodejsVersion", libs.versions.nodejs)
    outputs.dir("${project.buildDir}/nodejs")
}

tasks.installFrontend {
    inputs.apply {
        file("web/package.json")
        file("web/.yarnrc.yml")
    }
    outputs.apply {
        dir("web/node_modules")
        file("web/yarn.lock")
    }
}

tasks.assembleFrontend {
    inputs.apply {
        dir("web/src")
        dir("web/public")
        file("web/package.json")
        file("web/yarn.lock")
        file("web/.yarnrc.yml")
    }
    outputs.dir("web/build")
    doLast {
        if(outputs.files.asFileTree.none { it.isFile }) {
            throw GradleException("Failed to build frontend")
        }
    }
}

val copyWebApp = tasks.register<Copy>("copyWebApp") {
    group = "frontend"
    from("web/build")
    into("${project.buildDir}/resources/main/static/")
    dependsOn(tasks.assembleFrontend)
}

tasks.processResources.configure {
    dependsOn(copyWebApp)
}
