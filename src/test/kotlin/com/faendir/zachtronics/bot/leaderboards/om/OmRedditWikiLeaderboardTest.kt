package com.faendir.zachtronics.bot.leaderboards.om

import com.faendir.zachtronics.bot.BotTest
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.model.om.OmCategory
import com.faendir.zachtronics.bot.model.om.OmPuzzle
import com.faendir.zachtronics.bot.model.om.OmRecord
import com.faendir.zachtronics.bot.model.om.OmScore
import com.faendir.zachtronics.bot.model.om.OmScorePart.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.*

@BotTest
internal class OmRedditWikiLeaderboardTest {

    @Autowired
    lateinit var leaderboard: OmRedditWikiLeaderboard

    @Test
    fun get() {
        val record = leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.GC)
        expectThat(record).isNotNull()
        expectThat(record!!.score).isEqualTo(OmScore(COST to 40.0, CYCLES to 77.0, AREA to 9.0))
        expectThat(record.link).isEqualTo("https://i.imgur.com/lmvdNPM.gifv")
        expectThat(leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.HEIGHT)).isNull()
        expectThat(leaderboard.get(OmPuzzle.ABLATIVE_CRYSTAL, OmCategory.GC)).isNull()
    }

    @Test
    fun update1() {
        val record = OmRecord(OmScore(HEIGHT to 200.0, CYCLES to 200.0, COST to 200.0), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.STABILIZED_WATER, record)).isA<UpdateResult.NotSupported<*, *>>()
    }

    @Test
    fun update2() {
        val record = OmRecord(OmScore(COST to 200.0, CYCLES to 200.0, AREA to 200.0), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.ABLATIVE_CRYSTAL, record)).isA<UpdateResult.Success<*, *>>()
    }

    @Test
    fun update3() {
        val record = OmRecord(OmScore(COST to 200.0, CYCLES to 200.0, AREA to 200.0), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.STABILIZED_WATER, record)).isA<UpdateResult.BetterExists<*, *>>().and {
            get { scores.containsKey(OmCategory.GC) }.isTrue()
            get { scores[OmCategory.GC] }.isNotNull()
        }
    }
}