package com.faendir.zachtronics.bot.imgur

class TestImgurService : ImgurService {
    override fun tryRehost(link: String): String = link
}