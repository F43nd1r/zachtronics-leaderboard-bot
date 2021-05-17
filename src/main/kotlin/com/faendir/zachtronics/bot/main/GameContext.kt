package com.faendir.zachtronics.bot.main

import com.faendir.zachtronics.bot.generic.GenericConfiguration
import com.faendir.zachtronics.bot.generic.discord.Command
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Leaderboard
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class GameContext(marker: GamePackageMarker, parent: ApplicationContext) {
    companion object {
        private val logger = LoggerFactory.getLogger(GameContext::class.java)
    }

    private val id = marker.javaClass.name
    private val context = AnnotationConfigApplicationContext().apply {
        register(marker.packageConfiguration, GenericConfiguration::class.java)
        this.parent = parent
        refresh()
    }

    private inline fun <reified T> runInContext(block: ApplicationContext.() -> T): T {
        try {
            return context.block()
        } catch (e: Throwable) {
            logger.error("Failed to find bean of type ${T::class.java.name} in marker context $id", e)
            throw e
        }
    }

    val game: Game by lazy { runInContext { getBean(Game::class.java) } }

    val leaderboards: Collection<Leaderboard<*, *, *, *>> by lazy { runInContext { getBeansOfType(Leaderboard::class.java).values } }

    val commands: Collection<Command> by lazy { runInContext { getBeansOfType(Command::class.java).values } }
}