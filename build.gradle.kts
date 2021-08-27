import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

3plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
    id("org.springframework.boot") version "2.5.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("plugin.spring") version "1.5.30"
    id("com.palantir.docker") version "0.28.0"
    id("io.freefair.lombok") version "6.1.0"
    id("com.google.devtools.ksp") version "1.5.30-1.0.0-beta08"
    id("com.gorylenko.gradle-git-properties") version "2.3.1"
}

repositories {
    mavenCentral()
    google()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.discord4j:discord4j-core:3.2.0-RC3")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.4")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.12.0.202106070339-r")
    implementation("com.github.rockswang:java-curl:1.2.2.2")
    implementation("com.faendir.jraw:JRAW:1.2.0")
    implementation("net.bramp.ffmpeg:ffmpeg:0.6.2")
    implementation("com.faendir.om:dsl:1.2.6")
    implementation("com.faendir.om:parser:2.1.8")
    implementation("com.faendir.discord4j-command-parser:annotations:1.3.6")
    ksp("com.faendir.discord4j-command-parser:processor:1.3.6")
    implementation(project("native"))

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("net.sf.trove4j:trove4j:3.0.3")
    testImplementation("com.ninja-squad:springmockk:3.0.1")
    testImplementation("io.strikt:strikt-core:0.31.0")
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

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all", "-Xjsr305=strict")
    }
}

tasks.getByName<BootJar>("bootJar") {
    layered()
}

docker {
    name = "f43nd1r/om-leaderboard-discord-bot:latest"
    files(tasks.getByName<BootJar>("bootJar").outputs)
    copySpec.into("build/libs")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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
