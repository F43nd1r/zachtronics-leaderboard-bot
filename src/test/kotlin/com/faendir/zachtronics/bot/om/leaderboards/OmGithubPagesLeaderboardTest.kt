package com.faendir.zachtronics.bot.om.leaderboards

import com.faendir.zachtronics.bot.Application
import com.faendir.zachtronics.bot.BotTest
import com.faendir.zachtronics.bot.model.UpdateResult
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmScorePart.AREA
import com.faendir.zachtronics.bot.om.model.OmScorePart.COST
import com.faendir.zachtronics.bot.om.model.OmScorePart.CYCLES
import com.faendir.zachtronics.bot.om.model.OmScorePart.HEIGHT
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue

@BotTest(Application::class)
internal class OmGithubPagesLeaderboardTest {

    @Autowired
    lateinit var leaderboard: OmGithubPagesLeaderboard

    @Test
    fun get() {
        val record = leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.HEIGHT)
        expectThat(record).isNotNull()
        expectThat(record!!.score).isEqualTo(OmScore(HEIGHT to 1.0, CYCLES to 52.0, COST to 125.0))
        expectThat(record.link).isEqualTo("https://i.imgur.com/ZUxm30s.mp4")
        expectThat(leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.GC)).isNull()
        expectThat(leaderboard.get(OmPuzzle.ABLATIVE_CRYSTAL, OmCategory.HEIGHT)).isNull()
    }

    @Test
    fun update1() {
        val record = OmRecord(OmScore(AREA to 200.0, CYCLES to 200.0), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.STABILIZED_WATER, record)).isA<UpdateResult.NotSupported>()
    }

    @Test
    fun update2() {
        val record = OmRecord(OmScore(HEIGHT to 200.0, CYCLES to 200.0, COST to 200.0), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.ABLATIVE_CRYSTAL, record)).isA<UpdateResult.Success>()
    }

    @Test
    fun update3() {
        val record = OmRecord(OmScore(HEIGHT to 200.0, CYCLES to 200.0, COST to 200.0), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.STABILIZED_WATER, record)).isA<UpdateResult.BetterExists>().and {
            get { scores.containsKey(OmCategory.HEIGHT) }.isTrue()
            get { scores[OmCategory.HEIGHT] }.isNotNull()
        }
    }
}