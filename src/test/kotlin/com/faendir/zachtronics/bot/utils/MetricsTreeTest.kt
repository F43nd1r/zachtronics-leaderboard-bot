package com.faendir.zachtronics.bot.utils

import com.faendir.zachtronics.bot.om.model.OmCategory
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo


class MetricsTreeTest {

    @Test
    fun `should collapse metrics if fully present`() {
        val reference = setOf(OmCategory.CG, OmCategory.CA)
        val target = setOf(OmCategory.CG, OmCategory.CA)

        val result = target.smartFormat(reference)

        expectThat(result).isEqualTo("C")
    }

    @Test
    fun `should not collapse metrics if not fully present`() {
        val reference = setOf(OmCategory.CG, OmCategory.CA)
        val target = setOf(OmCategory.CG)

        val result = target.smartFormat(reference)

        expectThat(result).isEqualTo("CG")
    }

    @Test
    fun `should not collapse noCollapse metrics even if fully present`() {
        val reference = setOf(OmCategory.TIG)
        val target = setOf(OmCategory.TIG)

        val result = target.smartFormat(reference)
        expectThat(result).isEqualTo("TI")
    }
}