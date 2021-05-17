package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.OptionConverter
import com.faendir.zachtronics.bot.om.model.OmPuzzle

class PuzzleConverter : OptionConverter<OmPuzzle> {
    override fun fromString(string: String?): OmPuzzle = OmPuzzle.parse(string!!)
}