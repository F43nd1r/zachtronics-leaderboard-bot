package com.faendir.om.discord.model

interface Group<SELF : Group<SELF>> : Comparable<SELF> {
    val displayName: String
}