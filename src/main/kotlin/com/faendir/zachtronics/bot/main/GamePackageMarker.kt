package com.faendir.zachtronics.bot.main

interface GamePackageMarker {
    @JvmDefault
    val scanPackage: String
        get() = javaClass.packageName
}