package com.faendir.zachtronics.bot.utils

inline fun <T, R> Collection<T>.ifNotEmpty(block: (Collection<T>) -> R): R? = takeIf { it.isNotEmpty() }?.let(block)

inline fun <reified T> Iterable<*>.findInstance() : T? = filterIsInstance<T>().firstOrNull()

inline fun <reified T> Iterable<*>.findInstance(block: (T) -> Unit) = findInstance<T>()?.let(block)

fun <T : Map.Entry<K, V>, K, V> Iterable<T>.toMap() = map { it.key to it.value }.toMap()

fun <T> Iterable<T>.plusIf(condition: Boolean, element: T) : List<T> = if(condition) plus(element) else toList()