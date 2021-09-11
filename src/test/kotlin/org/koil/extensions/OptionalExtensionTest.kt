package org.koil.extensions

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.Test
import java.util.*

internal class OptionalExtensionTest {
    @Test
    fun `optional conversion to kotlin works for null and non-null`() {
        assertThat(Optional.empty<String>().toKotlin()).isNull()
        assertThat(Optional.of("Test").toKotlin()).isEqualTo("Test")
    }
}
