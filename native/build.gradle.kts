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

import org.gradle.internal.os.OperatingSystem
import java.net.URI

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradle.lombok)
}

group = ""


val omsimUrl =
    "https://github.com/ianh/omsim/releases/download/libverify-${OperatingSystem.current().familyName}-x86_64/libverify.so"
val omsimLib = layout.buildDirectory.file("downloaded/libverify-om.so")

val downloadFile by tasks.registering {
    outputs.file(omsimLib)
    doLast {
        val target = omsimLib.get().asFile
        target.parentFile.mkdirs()
        if (!target.exists()) {
            URI(omsimUrl).toURL().openStream().use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}

tasks.processResources {
    dependsOn(downloadFile)
    from(omsimLib) {
        into("lib")
    }
}

dependencies {
    implementation(libs.annotations)
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.launcher)
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.WARN // TODO
    }
}
