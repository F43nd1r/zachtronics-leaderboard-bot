package com.faendir.zachtronics.bot.model


interface Record {
    val score: Score
    fun toDisplayString(): String
}