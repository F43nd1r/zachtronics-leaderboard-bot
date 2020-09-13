package com.faendir.om.discord.model.om

import com.faendir.om.discord.model.Type

enum class OmType(override val displayName: String) : Type {
    NORMAL("normal"), INFINITE("infinite"), PRODUCTION("production"),
}