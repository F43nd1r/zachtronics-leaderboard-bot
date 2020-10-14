package com.faendir.zachtronics.bot.om.imgur

interface ImgurService {
    fun tryRehost(link: String): String
}