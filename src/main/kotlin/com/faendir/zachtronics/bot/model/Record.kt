package com.faendir.zachtronics.bot.model


interface Record {
    val score: Score
    val link: String
    val author: String?
    fun toDisplayString(): String = "${score.toDisplayString()}${author?.let { " by $it" } ?: ""} $link"
}