package com.faendir.zachtronics.bot.utils

sealed class Result<T> {
    class Success<T>(val result: T) : Result<T>()
    class Failure<T>(val message: String) : Result<T>()

    fun <R> map(block: (T) -> R): Result<R> = when (this) {
        is Success -> Success(block(this.result))
        is Failure -> @Suppress("UNCHECKED_CAST") (this as Failure<R>)
    }

    fun <R> flatMap(block: (T) -> Result<R>): Result<R> {
        return when (this) {
            is Success -> block(this.result)
            is Failure -> @Suppress("UNCHECKED_CAST") (this as Failure<R>)
        }
    }
}

val Result<String>.message
    get() = when (this) {
        is Result.Success -> result
        is Result.Failure -> message
    }