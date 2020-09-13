package com.faendir.om.discord.model

interface ScorePart<SELF : ScorePart<SELF>> : Comparable<SELF> {
    val key: Char
}