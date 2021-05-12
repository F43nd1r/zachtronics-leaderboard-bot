package com.faendir.zachtronics.bot.om.model

enum class OmMetric(val key: String, val canBePrimary: Boolean = true) {
    COST("G"),
    CYCLES("C"),
    AREA("A"),
    INSTRUCTIONS("I"),
    SUM("SUM"),
    SUM4("SUM4"),
    HEIGHT("H"),
    Width("W"),
    PRODUCT("X", false)

}