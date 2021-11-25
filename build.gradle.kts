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
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.kotlin.plugin.lombok)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
    alias(libs.plugins.gradle.docker)
    alias(libs.plugins.gradle.lombok)
    alias(libs.plugins.gradle.gitProperties)
    alias(libs.plugins.gradle.node)
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
            freeCompilerArgs = listOf("-Xjvm-default=all", "-Xjsr305=strict", "-Xopt-in=kotlin.RequiresOptIn")
        }
    }
}

dependencies {
    implementation(libs.kotlinx.json)
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
    implementation(projects.common)
    implementation(projects.native)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.kotlin)
    implementation(libs.springdoc)
    implementation(libs.guava)

    ksp(projects.processor)

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
            if(suite.parent == null) {
                println("Test results: ${result.resultType} (${result.testCount} tests,${result.successfulTestCount} successes, ${result.failedTestCount} " +
                        "failures, ${result.skippedTestCount} skipped)")
            }
        }
    })
}

docker {
    name = "f43nd1r/zachtronics-leaderboard-bot:latest"
    files(tasks.getByName<BootJar>("bootJar").outputs)
    copySpec.into("build/libs")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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

kotlinLombok {
    lombokConfigurationFile(file("lombok.config"))
}

afterEvaluate {
    // shut up gradle
    tasks.named("generateMainEffectiveLombokConfig2") {
        dependsOn(tasks.named("kspKotlin"))
    }
    tasks.named("generateTestEffectiveLombokConfig2") {
        dependsOn(tasks.named("kspTestKotlin"))
    }
}

node {
    download.set(Runtime.getRuntime().exec("node -v").waitFor() != 0)
    workDir.set(file("${project.buildDir}/nodejs"))
    yarnWorkDir.set(file("${project.buildDir}/yarn"))
    nodeProjectDir.set(file("web"))
}

val buildWebApp = tasks.register<com.github.gradle.node.yarn.task.YarnTask>("yarnBuild") {
    group = "yarn"
    args.set(listOf("run", "build"))
    inputs.dir("web/src")
    outputs.dir("web/build")
    dependsOn("yarn")
}

val copyWebApp = tasks.register<Copy>("copyWebApp") {
    from("web/build")
    into("${project.buildDir}/resources/main/static/")
    dependsOn(buildWebApp)
}

tasks.processResources.configure {
    dependsOn(copyWebApp)
}
