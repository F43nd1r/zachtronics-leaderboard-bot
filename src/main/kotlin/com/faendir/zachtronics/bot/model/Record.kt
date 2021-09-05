package com.faendir.zachtronics.bot.model

import reactor.util.function.Tuple2
import java.io.InputStream


interface Record {
    val score: Score
    val link: String
    val author: String?
    fun toShowDisplayString(): String = "${score.toDisplayString()}${author?.let { " by $it" } ?: ""} $link"
    fun toListDisplayString(): String = "[${score.toDisplayString()}](${link})"
    fun attachments(): List<Tuple2<String, InputStream>> = emptyList()
}