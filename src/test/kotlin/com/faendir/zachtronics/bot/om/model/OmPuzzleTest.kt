package com.faendir.zachtronics.bot.om.model

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import strikt.api.Assertion
import strikt.api.expectThat
import java.io.File

class OmPuzzleTest {
    @TestFactory
    fun `all puzzle files exist`() = OmPuzzle.entries.map { puzzle ->
        DynamicTest.dynamicTest(puzzle.displayName) {
            expectThat(puzzle.file).exists()
        }
    }
}

fun <T : File> Assertion.Builder<T>.exists() =
    assert("exists", expected = true) {
        if (it.exists()) pass(actual = it) else fail(actual = it)
    }