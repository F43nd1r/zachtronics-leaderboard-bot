package com.faendir.om.discord

import com.faendir.om.discord.jda.JdaService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Application(private val jdaService: JdaService) : CommandLineRunner {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }

    override fun run(vararg args: String?) {
        jdaService.start()
    }
}