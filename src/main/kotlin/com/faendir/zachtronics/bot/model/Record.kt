package com.faendir.zachtronics.bot.model


interface Record<S : Score> {
    val score: S
    fun toDisplayString(): String
}