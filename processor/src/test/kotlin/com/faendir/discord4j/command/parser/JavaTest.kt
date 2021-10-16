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

package com.faendir.discord4j.command.parser

import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Test

class JavaTest {

    @Test
    fun `usage`() {
        compileJava(
            SourceFile.java(
                "Company.java",
                """
                public enum Company {
                    A, B, X
                }
                """
            ),
            SourceFile.java(
            "Sample.java",
            """
                import com.faendir.discord4j.command.annotation.ApplicationCommand;
                import org.jetbrains.annotations.NotNull;
                @ApplicationCommand(name = "Sam", subCommand = true)
                public class Sample {
                    private final String name;
                    private final int age;
                    private final boolean male;
                    private final Company companyName;
                    
                    public Sample(@NotNull String name, int age, boolean male, Company companyName) {
                        this.name = name;
                        this.age = age;
                        this.male = male;
                        this.companyName = companyName;
                    }
                }
            """),
            eval = """
                import strikt.api.expectThat
                import strikt.assertions.isEqualTo
                fun test() {
                    expectThat(SampleParser.buildData()) {
                        get { options().get().size }.isEqualTo(4)
                        get { name() }.isEqualTo("Sam")
                    }
                }
            """
        )
    }
}