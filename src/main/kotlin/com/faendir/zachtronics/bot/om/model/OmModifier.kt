package com.faendir.zachtronics.bot.om.model

enum class OmModifier(val key: Char) {
    //order is important! Every value is a superset of the values to the left
    TRACKLESS('t'), NORMAL('?'), OVERLAP('o')
}