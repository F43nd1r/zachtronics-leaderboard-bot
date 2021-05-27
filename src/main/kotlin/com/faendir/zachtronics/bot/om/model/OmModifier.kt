package com.faendir.zachtronics.bot.om.model

enum class OmModifier(val displayName: String) {
    //order is important! Every value is a superset of the values to the left
    TRACKLESS("T"), NORMAL(""), OVERLAP("O")
}