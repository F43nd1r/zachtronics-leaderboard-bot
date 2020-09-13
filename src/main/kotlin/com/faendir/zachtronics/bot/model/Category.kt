package com.faendir.zachtronics.bot.model

interface Category<SELF : Category<SELF, S, P>, S : Score<S, P>, P : ScorePart<P>> : Comparable<SELF> {
    val name: String
    val displayName: String
    val requiredParts: List<P>
    val supportedTypes: Set<Type>
    val supportedGroups: Set<Group<*>>
    fun normalizeScore(score: S): S {
        check(score.parts.keys.containsAll(requiredParts))
        return score.mutate(requiredParts.map { it to score.parts[it]!! }.toMap(LinkedHashMap()))
    }

    fun isBetterOrEqual(s1: S, s2: S): Boolean
}