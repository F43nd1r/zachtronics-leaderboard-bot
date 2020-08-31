package com.faendir.om.discord.model

import kotlinx.serialization.Serializable

@Serializable
data class Record(val category: Category, val score: Score, val link: String)