package com.faendir.zachtronics.bot.main

import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

@Configuration
@ComponentScan(basePackages = ["com.faendir.zachtronics.bot"],
    useDefaultFilters = false,
    includeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [GamePackageMarker::class])])
class GameConfiguration(applicationContext: ApplicationContext, @Suppress("SpringJavaInjectionPointsAutowiringInspection") markers: List<GamePackageMarker>) {
    @get:Bean
    val contexts: List<GameContext> = markers.map { GameContext(it, applicationContext) }
}