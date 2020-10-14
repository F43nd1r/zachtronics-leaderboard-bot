package com.faendir.zachtronics.bot.om.imgur

class TestImgurService : ImgurService {
    override fun tryRehost(link: String): String = link
}