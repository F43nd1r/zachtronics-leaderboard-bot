package com.faendir.zachtronics.bot.model

import reactor.util.function.Tuple2
import java.io.InputStream


interface Record {
    val score: Score
    val link: String
    val author: String?
    fun toDisplayString(): String = "${score.toDisplayString()}${author?.let { " by $it" } ?: ""} $link"
    fun attachments(): List<Tuple2<String, InputStream>> = emptyList()
}