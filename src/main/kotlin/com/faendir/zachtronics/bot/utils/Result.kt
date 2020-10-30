package com.faendir.zachtronics.bot.utils

sealed class Result<T> {
    companion object {
        @JvmStatic
        fun <T> success(result: T): Result<T> = Success(result)

        @JvmStatic
        fun <T> failure(message: String): Result<T> = Failure(message)

        @JvmStatic
        @JvmOverloads
        fun <T> parseFailure(message: String, fix: String = "fix that") = failure<T>("sorry, $message\n\nYou can edit your command to $fix.")
    }

    @PublishedApi
    internal class Success<T>(val result: T) : Result<T>()

    @PublishedApi
    internal class Failure<T>(val message: String) : Result<T>()

    inline fun <R> fold(onSuccess: (T) -> R, onFailure: (String) -> R): R {
        return when (this) {
            is Success -> onSuccess(this.result)
            is Failure -> onFailure(message)
        }
    }

    inline fun <R> map(block: (T) -> R): Result<R> = flatMap { Success(block(it)) }

    inline fun <R> flatMap(block: (T) -> Result<R>): Result<R> = fold(block, { @Suppress("UNCHECKED_CAST") (this as Failure<R>) })

    inline fun onFailure(block: (String) -> T): T {
        return fold({ it }, { block(it) })
    }

    inline fun mapFailure(block: (String) -> Result<T>): Result<T> {
        return fold({ this }, { block(it) })
    }

    inline fun onFailureTry(block: (String) -> Result<T>) : Result<T> {
        return fold({ this }, { block(it).mapFailure { this }})
    }
}

val Result<String>.message
    get() = fold({ it }, { it })

fun <T, U, V> Result<Pair<T, U>>.and(block: (T, U) -> Result<V>): Result<Triple<T, U, V>> = flatMap { pair ->
    block(pair.first, pair.second).map { Triple(pair.first, pair.second, it) }
}

fun <T, U> Result<T>.and(block: (T) -> Result<U>): Result<Pair<T, U>> = flatMap { result -> block(result).map { result to it } }