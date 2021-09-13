package org.koil.extensions

import java.util.*

fun <T> Optional<T>.toKotlin(): T? = if (this.isPresent) {
    this.get()
} else {
    null
}
