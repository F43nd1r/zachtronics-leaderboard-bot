package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Solution

data class OmSolution(val puzzle: OmPuzzle, override val score: OmScore, val solution: String) : Solution
