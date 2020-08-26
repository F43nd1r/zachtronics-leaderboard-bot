package com.faendir.om.discord.utils

import com.faendir.om.discord.categories.ScorePart
import java.text.DecimalFormat

typealias Score = List<Pair<ScorePart, Double>>

private val numberFormat = DecimalFormat("0.#")
fun Score.toScoreString(separator: String, includeSuffix: Boolean = true) = toList().joinToString(separator) {
    val num = numberFormat.format(it.second)
    if (includeSuffix) num + it.first.key else num
}
