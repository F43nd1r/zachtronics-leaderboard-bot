package com.faendir.zachtronics.bot.model.om

import com.faendir.zachtronics.bot.model.Type

enum class OmType(override val displayName: String) : Type {
    NORMAL("normal"), INFINITE("infinite"), PRODUCTION("production"),
}