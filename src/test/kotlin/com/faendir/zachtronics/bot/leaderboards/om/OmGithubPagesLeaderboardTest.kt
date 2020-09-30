package com.faendir.zachtronics.bot.leaderboards.om

import com.faendir.zachtronics.bot.TestConfiguration
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.model.om.OmCategory
import com.faendir.zachtronics.bot.model.om.OmPuzzle
import com.faendir.zachtronics.bot.model.om.OmRecord
import com.faendir.zachtronics.bot.model.om.OmScore
import com.faendir.zachtronics.bot.model.om.OmScorePart.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import strikt.api.expectThat
import strikt.assertions.*

@SpringBootTest
@ContextConfiguration(classes = [TestConfiguration::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
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
        val record = OmRecord(OmScore(AREA to 200.0, CYCLES to 200.0, COST to 200.0), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.STABILIZED_WATER, record)).isA<UpdateResult.NotSupported<*, *>>()
    }

    @Test
    fun update2() {
        val record = OmRecord(OmScore(HEIGHT to 200.0, CYCLES to 200.0, COST to 200.0), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.ABLATIVE_CRYSTAL, record)).isA<UpdateResult.Success<*, *>>()
    }

    @Test
    fun update3() {
        val record = OmRecord(OmScore(HEIGHT to 200.0, CYCLES to 200.0, COST to 200.0), "http://fake.link")
        expectThat(leaderboard.update(OmPuzzle.STABILIZED_WATER, record)).isA<UpdateResult.BetterExists<*, *>>().and {
            get { scores.containsKey(OmCategory.HEIGHT) }.isTrue()
            get { scores[OmCategory.HEIGHT] }.isNotNull()
        }
    }
}