package org.koil.extensions

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

fun <T> T.toSuccess(): Success<T> = Success(this)

fun <E> E.toFailure(): Failure<E> = Failure(this)

fun <T, E> Result<T, E>.getOrThrow(e: IllegalStateException = IllegalStateException("Result not successful. Cannot unwrap.")): T =
    when (this) {
        is Success -> this.value
        is Failure -> throw e
    }

fun <T, E> Result<T, E>.isSuccess(): Boolean = this is Success
