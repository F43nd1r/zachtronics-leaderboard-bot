package com.faendir.zachtronics.bot.om.model

enum class OmMetric(val key: String, val scorePart: OmScorePart) {
    COST("G", OmScorePart.COST),
    CYCLES("C", OmScorePart.CYCLES),
    AREA("A",OmScorePart.AREA),
    INSTRUCTIONS("I", OmScorePart.INSTRUCTIONS),
    SUM("SUM", OmScorePart.COMPUTED),
    SUM4("SUM4", OmScorePart.COMPUTED),
    HEIGHT("H", OmScorePart.HEIGHT),
    Width("W", OmScorePart.WIDTH),
    PRODUCT("X", OmScorePart.COMPUTED)

}