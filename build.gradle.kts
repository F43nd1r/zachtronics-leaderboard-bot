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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

@Suppress("DSL_SCOPE_VIOLATION") // TODO remove when https://youtrack.jetbrains.com/issue/KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.kotlin.plugin.lombok)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
    alias(libs.plugins.gradle.docker)
    alias(libs.plugins.gradle.lombok)
    alias(libs.plugins.gradle.gitProperties)
    alias(libs.plugins.gradle.frontend)
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
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

docker {
    name = "f43nd1r/zachtronics-leaderboard-bot"
    tag("latest", "docker.io/f43nd1r/zachtronics-leaderboard-bot")
    files(tasks.getByName<BootJar>("bootJar").outputs)
    copySpec.into("build/libs")
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
    yarnEnabled.set(true)
    yarnVersion.set(libs.versions.yarn)
    packageJsonDirectory.set(file("web"))
    assembleScript.set("build")
    cleanScript.set("clean")
}

tasks.installNode {
    inputs.property("nodejsVersion", libs.versions.nodejs)
    outputs.dir("${project.buildDir}/nodejs")
}

tasks.installYarnGlobally {
    onlyIf { !file("${project.buildDir}/nodejs/lib/node_modules/yarn").exists() }
}

tasks.enableYarnBerry {
    enabled = false
}

tasks.installYarn {
    inputs.property("yarnVersion", libs.versions.yarn)
    outputs.dir("web/.yarn/releases")
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
}

val copyWebApp = tasks.register<Copy>("copyWebApp") {
    from("web/build")
    into("${project.buildDir}/resources/main/static/")
    dependsOn(tasks.assembleFrontend)
}

tasks.processResources.configure {
    dependsOn(copyWebApp)
}
