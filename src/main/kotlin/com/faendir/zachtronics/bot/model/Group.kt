package com.faendir.zachtronics.bot.model

interface Group<SELF : Group<SELF>> : Comparable<SELF> {
    val displayName: String
}