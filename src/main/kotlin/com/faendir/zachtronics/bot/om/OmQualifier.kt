package com.faendir.zachtronics.bot.om

import org.springframework.beans.factory.annotation.Qualifier

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class OmQualifier
