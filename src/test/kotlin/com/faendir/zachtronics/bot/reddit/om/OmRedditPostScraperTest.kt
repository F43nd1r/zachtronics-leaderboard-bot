package com.faendir.zachtronics.bot.reddit.om

import com.faendir.zachtronics.bot.TestConfiguration
import com.faendir.zachtronics.bot.leaderboards.om.OmRedditWikiLeaderboard
import com.faendir.zachtronics.bot.model.om.OmCategory
import com.faendir.zachtronics.bot.model.om.OmPuzzle
import com.faendir.zachtronics.bot.model.om.OmScorePart
import com.faendir.zachtronics.bot.reddit.Comment
import com.faendir.zachtronics.bot.reddit.RedditService
import com.faendir.zachtronics.bot.utils.Forest
import com.faendir.zachtronics.bot.utils.Tree
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNotNull
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.*

@SpringBootTest
@ContextConfiguration(classes = [TestConfiguration::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
internal class OmRedditPostScraperTest {

    @Autowired
    lateinit var scraper: OmRedditPostScraper

    @Autowired
    lateinit var leaderboard: OmRedditWikiLeaderboard

    @SpykBean
    lateinit var redditService: RedditService

    @Test
    fun oldComment() {
        val lastUpdate = Date()
        val comment = Comment("0",
            "${OmPuzzle.STABILIZED_WATER.displayName}: [1/1/1](http://fake.link)",
            "trustedUser1",
            lastUpdate.minus(5, ChronoUnit.MINUTES),
            null)
        scraper.handleComment(lastUpdate, singletonForest(comment), comment)
        expectThat(leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.GC)).isNotNull().and {
            get { score.parts[OmScorePart.COST] }.isNotEqualTo(1.0)
        }
        verify(exactly = 0) { redditService.reply(comment, any()) }
    }

    @Test
    fun newComment() {
        val lastUpdate = Date()
        val comment = Comment("0",
            "${OmPuzzle.STABILIZED_WATER.displayName}: [1/1/1](http://fake.link)",
            "trustedUser1",
            lastUpdate.plus(5, ChronoUnit.MINUTES),
            null)
        scraper.handleComment(lastUpdate, singletonForest(comment), comment)
        expectThat(leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.GC)).isNotNull().and {
            get { score.parts[OmScorePart.COST] }.isEqualTo(1.0)
        }
        verify(exactly = 1) { redditService.reply(comment, any()) }
    }

    @Test
    fun updatedComment() {
        val lastUpdate = Date()
        val comment = Comment("0",
            "${OmPuzzle.STABILIZED_WATER.displayName}: [1/1/1](http://fake.link)",
            "trustedUser1",
            lastUpdate.minus(5, ChronoUnit.MINUTES),
            lastUpdate.plus(5, ChronoUnit.MINUTES))
        scraper.handleComment(lastUpdate, singletonForest(comment), comment)
        expectThat(leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.GC)).isNotNull().and {
            get { score.parts[OmScorePart.COST] }.isEqualTo(1.0)
        }
        verify(exactly = 1) { redditService.reply(comment, any()) }
    }

    @Test
    fun commentWithOtherText() {
        val lastUpdate = Date()
        val comment = Comment("0",
            "Here's some text.\n${OmPuzzle.STABILIZED_WATER.displayName}: [1/1/1](http://fake.link)\n This is some more text.\nIt should be ignored.",
            "trustedUser1",
            lastUpdate.plus(5, ChronoUnit.MINUTES),
            null)
        scraper.handleComment(lastUpdate, singletonForest(comment), comment)
        expectThat(leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.GC)).isNotNull().and {
            get { score.parts[OmScorePart.COST] }.isEqualTo(1.0)
        }
        verify(exactly = 1) { redditService.reply(comment, any()) }
    }

    @Test
    fun multipleSubmissionLine() {
        val lastUpdate = Date()
        val comment = Comment("0",
            "${OmPuzzle.STABILIZED_WATER.displayName}: [1/2/3](http://fake.link), [3/2/1](http://fake.link)",
            "trustedUser1",
            lastUpdate.plus(5, ChronoUnit.MINUTES),
            null)
        scraper.handleComment(lastUpdate, singletonForest(comment), comment)
        expectThat(leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.GC)).isNotNull().and {
            get { score.parts[OmScorePart.COST] }.isEqualTo(1.0)
            get { score.parts[OmScorePart.AREA] }.isEqualTo(3.0)
        }
        expectThat(leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.AG)).isNotNull().and {
            get { score.parts[OmScorePart.AREA] }.isEqualTo(1.0)
            get { score.parts[OmScorePart.COST] }.isEqualTo(3.0)
        }
        verify(exactly = 1) { redditService.reply(comment, any()) }
    }

    @Test
    fun multiLineSubmission() {
        val lastUpdate = Date()
        val comment = Comment("0",
            "${OmPuzzle.STABILIZED_WATER.displayName}: [1/1/1](http://fake.link)\n${OmPuzzle.ABLATIVE_CRYSTAL.displayName}: [1/1/1](http://fake.link)",
            "trustedUser1",
            lastUpdate.plus(5, ChronoUnit.MINUTES),
            null)
        scraper.handleComment(lastUpdate, singletonForest(comment), comment)
        expectThat(leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.GC)).isNotNull().and {
            get { score.parts[OmScorePart.COST] }.isEqualTo(1.0)
        }
        expectThat(leaderboard.get(OmPuzzle.ABLATIVE_CRYSTAL, OmCategory.GC)).isNotNull().and {
            get { score.parts[OmScorePart.COST] }.isEqualTo(1.0)
        }
        verify(exactly = 1) { redditService.reply(comment, any()) }
    }

    @Test
    fun untrustedUser() {
        val lastUpdate = Date()
        val comment = Comment("0",
            "${OmPuzzle.STABILIZED_WATER.displayName}: [1/1/1](http://fake.link)",
            "untrustedUser",
            lastUpdate.plus(5, ChronoUnit.MINUTES),
            null)
        scraper.handleComment(lastUpdate, singletonForest(comment), comment)
        expectThat(leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.GC)).isNotNull().and {
            get { score.parts[OmScorePart.COST] }.isNotEqualTo(1.0)
        }
        verify(exactly = 1) { redditService.reply(comment, any()) }
    }

    @Test
    fun unrelatedTrustedComment() {
        val lastUpdate = Date()
        val comment = Comment("0",
            "Talking about something",
            "trustedUser1",
            lastUpdate.plus(5, ChronoUnit.MINUTES),
            null)
        scraper.handleComment(lastUpdate, singletonForest(comment), comment)
        expectThat(leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.GC)).isNotNull().and {
            get { score.parts[OmScorePart.COST] }.isNotEqualTo(1.0)
        }
        verify(exactly = 0) { redditService.reply(comment, any()) }
    }

    @Test
    fun unrelatedUnTrustedComment() {
        val lastUpdate = Date()
        val comment = Comment("0",
            "Talking about something",
            "untrustedUser",
            lastUpdate.plus(5, ChronoUnit.MINUTES),
            null)
        scraper.handleComment(lastUpdate, singletonForest(comment), comment)
        expectThat(leaderboard.get(OmPuzzle.STABILIZED_WATER, OmCategory.GC)).isNotNull().and {
            get { score.parts[OmScorePart.COST] }.isNotEqualTo(1.0)
        }
        verify(exactly = 0) { redditService.reply(comment, any()) }
    }

    fun Date.minus(subtract: Long, unit: TemporalUnit): Date = Date.from(toInstant().minus(subtract, unit))

    fun Date.plus(subtract: Long, unit: TemporalUnit): Date = Date.from(toInstant().plus(subtract, unit))

    fun <T> singletonForest(element: T): Forest<T> = Forest(listOf(Tree(element, listOf())))

}