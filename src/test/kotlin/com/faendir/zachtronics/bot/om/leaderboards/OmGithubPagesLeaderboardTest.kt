package com.faendir.zachtronics.bot.om.leaderboards

import com.faendir.zachtronics.bot.BotTest
import com.faendir.zachtronics.bot.model.UpdateResult
import com.faendir.zachtronics.bot.om.OpusMagnumConfiguration
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmScorePart.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.*

@BotTest(OpusMagnumConfiguration::class)
internal class OmGithubPagesLeaderboardTest {

    @Autowired
    lateinit var leaderboard: OmGithubPagesLeaderboard

    @Test
    fun get() {
        val record = leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.HEIGHT).block()
        expectThat(record).isNotNull()
        expectThat(record!!.score).isEqualTo(OmScore(HEIGHT to 1.0, CYCLES to 52.0, COST to 125.0))
        expectThat(record.link).isEqualTo("https://i.imgur.com/ZUxm30s.mp4")
        expectThat(leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.GC).block()).isNull()
        expectThat(leaderboard.get(OmPuzzle.ABLATIVE_CRYSTAL, OmCategory.HEIGHT).block()).isNull()
    }

    @Test
    fun update1() {
        val record = OmRecord(OmScore(AREA to 200.0, CYCLES to 200.0), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.STABILIZED_WATER, record).block()).isA<UpdateResult.NotSupported>()
    }

    @Test
    fun update2() {
        val record = OmRecord(OmScore(HEIGHT to 200.0, CYCLES to 200.0, COST to 200.0), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.ABLATIVE_CRYSTAL, record).block()).isA<UpdateResult.Success>()
    }

    @Test
    fun update3() {
        val record = OmRecord(OmScore(HEIGHT to 200.0, CYCLES to 200.0, COST to 200.0), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.STABILIZED_WATER, record).block()).isA<UpdateResult.BetterExists>().and {
            get { scores.containsKey(OmCategory.HEIGHT) }.isTrue()
            get { scores[OmCategory.HEIGHT] }.isNotNull()
        }
    }
}