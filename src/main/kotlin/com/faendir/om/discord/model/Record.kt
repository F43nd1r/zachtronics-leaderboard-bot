package com.faendir.om.discord.model

interface Record {
    val category: Category<*, *, *>
    val score: Score<*, *>
    val link: String
}