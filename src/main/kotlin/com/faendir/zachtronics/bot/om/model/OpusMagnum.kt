package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.utils.getSingleMatchingPuzzle
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.User
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class OpusMagnum : Game<OmCategory, OmScore, OmPuzzle, OmRecord> {

    override val discordChannel = "opus-magnum"

    override val displayName = "Opus Magnum"

    override val commandName = "om"

    override fun parsePuzzle(name: String): OmPuzzle = OmPuzzle.values().getSingleMatchingPuzzle(name)

    internal fun parseScore(puzzle: OmPuzzle, string: String): OmScore {
        if (string.isBlank()) throw IllegalArgumentException("I didn't find a score in your command.")
        val outerParts = string.split(':')
        val (modifier, scoreString) = when (outerParts.size) {
            1 -> null to string
            2 -> (OmModifier.values().find { it.key.toString() == outerParts[0] }
                ?: throw IllegalArgumentException("\"${outerParts[0]}\" is not a modifier.")) to outerParts[1]
            else -> throw IllegalArgumentException("I didn't understand \"$string\".")
        }
        val parts = scoreString.split('/', '-')
        if (parts.size < 3) throw IllegalArgumentException("your score must have at least three parts.")
        if (string.contains(Regex("[a-zA-Z]"))) {
            return OmScore(parts.map { OmScorePart.parse(it) ?: throw IllegalArgumentException("I didn't understand \"$it\".") }, modifier)
        }
        if (parts.size == 4) {
            return OmScore(OmScorePart.COST to parts[0].toDouble(),
                OmScorePart.CYCLES to parts[1].toDouble(),
                OmScorePart.AREA to parts[2].toDouble(),
                OmScorePart.INSTRUCTIONS to parts[3].toDouble(),
                modifier = modifier)
        }
        if (parts.size == 3) {
            return OmScore(OmScorePart.COST to parts[0].toDouble(),
                OmScorePart.CYCLES to parts[1].toDouble(),
                (if (puzzle.type == OmType.PRODUCTION) OmScorePart.INSTRUCTIONS else OmScorePart.AREA) to parts[2].toDouble(),
                modifier = modifier)
        }
        throw IllegalArgumentException("you need to specify score part identifiers when using more than four values.")
    }

    override fun parseCategory(name: String): List<OmCategory> =
        OmCategory.values().filter { category -> category.displayNames.any { it.equals(name, ignoreCase = true) } }

    override fun hasWritePermission(user: User): Mono<Boolean> {
        return if(user is Member) user.roles.any { it.name == "trusted-leaderboard-poster" } else false.toMono()
    }
}