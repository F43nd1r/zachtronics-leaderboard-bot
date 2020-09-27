package com.faendir.zachtronics.bot.utils

sealed class Result<T> {
    class Success<T>(val result: T) : Result<T>()
    class Failure<T>(val message: String) : Result<T>()

    fun <R> map(block: (T) -> R): Result<R> = when (this) {
        is Success -> Success(block(this.result))
        is Failure -> @Suppress("UNCHECKED_CAST") (this as Failure<R>)
    }

    fun <R> flatMap(block: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> block(this.result)
        is Failure -> @Suppress("UNCHECKED_CAST") (this as Failure<R>)
    }

    inline fun onFailure(block: () -> T): T = when (this) {
        is Success -> this.result
        is Failure -> block()
    }
}

val Result<String>.message
    get() = when (this) {
        is Result.Success -> result
        is Result.Failure -> message
    }

fun <T, U, V> Result<Pair<T, U>>.and(block: (T, U) -> Result<V>): Result<Triple<T, U, V>> = when (this) {
    is Result.Success -> block(this.result.first, this.result.second).map { Triple(this.result.first, this.result.second, it) }
    is Result.Failure -> @Suppress("UNCHECKED_CAST") (this as Result.Failure<Triple<T, U, V>>)
}

fun <T, U> Result<T>.and(block: (T) -> Result<U>): Result<Pair<T, U>> = when (this) {
    is Result.Success -> block(this.result).map { this.result to it }
    is Result.Failure -> @Suppress("UNCHECKED_CAST") (this as Result.Failure<Pair<T, U>>)
}