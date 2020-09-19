package com.faendir.zachtronics.bot.model

interface Record {
    val category: Category<*, *, *>
    val score: Score
    val link: String
}