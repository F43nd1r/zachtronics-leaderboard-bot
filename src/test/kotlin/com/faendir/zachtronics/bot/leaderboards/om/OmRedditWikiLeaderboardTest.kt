package com.faendir.zachtronics.bot.leaderboards.om

import com.faendir.zachtronics.bot.TestConfiguration
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.model.om.*
import com.faendir.zachtronics.bot.model.om.OmScorePart.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import strikt.api.expectThat
import strikt.assertions.*

@SpringBootTest
@ContextConfiguration(classes = [TestConfiguration::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
internal class OmRedditWikiLeaderboardTest {

    @Autowired
    lateinit var leaderboard: OmRedditWikiLeaderboard

    @Test
    fun get() {
        val record = leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.GC)
        expectThat(record).isNotNull()
        expectThat(record!!.score).isEqualTo(OmScore(mapOf(COST to 40.0, CYCLES to 77.0, AREA to 9.0).toMap(LinkedHashMap
            ())))
        expectThat(record.link).isEqualTo("https://i.imgur.com/lmvdNPM.gifv")
        expectThat(leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.HEIGHT)).isNull()
        expectThat(leaderboard.get(OmPuzzle.ABLATIVE_CRYSTAL, OmCategory.GC)).isNull()
    }

    @Test
    fun update1() {
        val record = OmRecord(OmScore(mapOf(HEIGHT to 200.0, CYCLES to 200.0, COST to 200.0).toMap(LinkedHashMap())), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.STABILIZED_WATER, record)).isA<UpdateResult.NotSupported<*, *>>()
    }

    @Test
    fun update2() {
        val record = OmRecord(OmScore(mapOf(COST to 200.0, CYCLES to 200.0, AREA to 200.0).toMap(LinkedHashMap())), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.ABLATIVE_CRYSTAL, record)).isA<UpdateResult.Success<*, *>>()
    }

    @Test
    fun update3() {
        val record = OmRecord(OmScore(mapOf(COST to 200.0, CYCLES to 200.0, AREA to 200.0).toMap(LinkedHashMap())), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.STABILIZED_WATER, record)).isA<UpdateResult.BetterExists<*, *>>().and {
            get { scores.containsKey(OmCategory.GC) }.isTrue()
            get { scores[OmCategory.GC] }.isNotNull()
        }
    }
}