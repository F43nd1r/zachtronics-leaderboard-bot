package com.faendir.om.discord.model

interface Puzzle {
    val group: Group<*>
    val type: Type
    val displayName: String
}