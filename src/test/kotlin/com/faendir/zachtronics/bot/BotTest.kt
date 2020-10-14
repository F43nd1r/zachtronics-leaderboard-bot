package com.faendir.zachtronics.bot

import com.faendir.zachtronics.bot.generic.GenericConfiguration
import com.faendir.zachtronics.bot.main.Application
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.annotation.AliasFor
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.AnnotationConfigContextLoader
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(initializers = [ContextInitializer::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
annotation class BotTest(@get:AliasFor(annotation = ContextConfiguration::class, attribute = "classes") val value: KClass<*> = Unit::class)

class ContextInitializer : ApplicationContextInitializer<AnnotationConfigApplicationContext> {
    override fun initialize(applicationContext: AnnotationConfigApplicationContext) {
        applicationContext.register(Application::class.java, GenericConfiguration::class.java, TestConfiguration::class.java)
    }
}