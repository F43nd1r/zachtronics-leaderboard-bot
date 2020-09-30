package com.faendir.zachtronics.bot.imgur

interface ImgurService {
    fun tryRehost(link: String): String
}