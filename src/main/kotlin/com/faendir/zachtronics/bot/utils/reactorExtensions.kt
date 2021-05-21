package com.faendir.zachtronics.bot.utils

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.cast
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

fun <T> Mono<T?>.throwIfEmpty(message: () -> String): Mono<T> = switchIfEmpty { throw IllegalArgumentException(message()) }.map { it as T }

fun <T : Any, U : Any, R : Any> Mono<Tuple2<T, U>>.flatMapFirst(map: (T, U) -> Mono<R>): Mono<Tuple2<R, U>> =
    flatMap { (t, u) -> map(t, u).zipWith(u.toMono()) }

fun <T : Any, U : Any, R : Any> Mono<Tuple2<T, U>>.flatMapSecond(map: (T, U) -> Mono<R>): Mono<Tuple2<T, R>> =
    flatMap { (t, u) -> t.toMono().zipWith(map(t, u)) }

fun <T, R> Flux<T>.kMapNotNull(map: (T) -> R?): Flux<R> = mapNotNull(map)

fun <T, R> Mono<T>.kMapNotNull(map: (T) -> R?): Mono<R> = mapNotNull(map)

fun Mono<Boolean>.ifTrue(action: () -> Unit): Mono<Boolean> = doOnSuccess { if (it) action() }

inline fun <reified R : Any> Flux<*>.filterIsInstance() = filter { it is R }.cast<R>()

inline fun <reified R : Any> Mono<*>.filterIsInstance(): Mono<R?> = filter { it is R }.cast(R::class.java)