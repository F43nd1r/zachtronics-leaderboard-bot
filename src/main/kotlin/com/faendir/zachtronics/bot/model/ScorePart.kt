package com.faendir.zachtronics.bot.model

interface ScorePart<SELF : ScorePart<SELF>> : Comparable<SELF> {
    val key: Char
}