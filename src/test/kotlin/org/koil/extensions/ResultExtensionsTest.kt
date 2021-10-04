package org.koil.extensions

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ResultExtensionsTest {
    @Test
    fun `wrapping in success`() {
        val result = "Hello".toSuccess()

        assertThat(result).isEqualTo(Success("Hello"))
    }

    @Test
    internal fun `wrapping in failure`() {
        val result = "Hello".toFailure()

        assertThat(result).isEqualTo(Failure("Hello"))
    }

    @Test
    internal fun `unwrapping a successful result`() {
        val result = "Hello".toSuccess()

        assertThat(result.getOrThrow()).isEqualTo("Hello")
    }


    @Test
    internal fun `unwrapping a failure result`() {
        val result = "Hello".toFailure()

        assertThrows<IllegalStateException> {
            result.getOrThrow()
        }
    }

    @Test
    internal fun `checking is success`() {
        val success = "".toSuccess()
        val failure = "".toFailure()

        assertTrue(success.isSuccess())
        assertFalse(failure.isSuccess())
    }
}
