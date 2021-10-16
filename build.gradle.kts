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

plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    id("org.springframework.boot") version "2.5.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("plugin.spring") version "1.5.31"
    id("com.palantir.docker") version "0.28.0"
    id("io.freefair.lombok") version "6.1.0"
    id("com.google.devtools.ksp") version "1.5.31-1.0.0"
    id("com.gorylenko.gradle-git-properties") version "2.3.1"
}

allprojects {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "16"
            freeCompilerArgs = listOf("-Xjvm-default=all", "-Xjsr305=strict")
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.cloud:spring-cloud-starter:3.0.4")
    implementation("com.discord4j:discord4j-core:3.2.0")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.4")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.13.0.202109080827-r")
    implementation("com.github.rockswang:java-curl:1.2.2.2")
    implementation("com.faendir.jraw:JRAW:1.2.0")
    implementation("net.bramp.ffmpeg:ffmpeg:0.6.2")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.faendir.om:dsl:1.2.6")
    implementation("com.faendir.om:parser:2.1.8")
    implementation(project("common"))
    ksp(project("processor"))
    implementation(project("native"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("net.sf.trove4j:trove4j:3.0.3")
    testImplementation("com.ninja-squad:springmockk:3.0.1")
    testImplementation("io.strikt:strikt-core:0.32.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}

        override fun beforeTest(testDescriptor: TestDescriptor) {}

        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}

        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if(suite.parent == null) {
                println("Test results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} " +
                        "failures, ${result.skippedTestCount} skipped)")
            }
        }
    })
}

docker {
    name = "f43nd1r/om-leaderboard-discord-bot:latest" // TODO change name to zachtronics-leaderboard-bot
    files(tasks.getByName<BootJar>("bootJar").outputs)
    copySpec.into("build/libs")
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

sourceSets {
    main {
        java {
            srcDir(file("$buildDir/generated/ksp/main/kotlin"))
        }
    }
    test {
        java {
            srcDir(file("$buildDir/generated/ksp/test/kotlin"))
        }
    }
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

afterEvaluate {
    // shut up gradle
    tasks.named("generateMainEffectiveLombokConfig2") {
        dependsOn(tasks.named("kspKotlin"))
    }
}
