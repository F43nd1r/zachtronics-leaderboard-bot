package com.faendir.zachtronics.bot.om

import com.faendir.zachtronics.bot.main.GamePackageMarker
import org.springframework.context.annotation.ComponentScan

@Suppress("unused")
class OpusMagnumMarker : GamePackageMarker {
    override val packageConfiguration: Class<*> = OpusMagnumConfiguration::class.java
}

@ComponentScan
class OpusMagnumConfiguration
