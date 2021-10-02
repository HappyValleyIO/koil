package org.koil.user

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class HashedPasswordTest {
    @Test
    internal fun `unencoded password cannot be wrapped`() {
        assertThrows<IllegalArgumentException> {
            HashedPassword("Unencoded!")
        }
    }
}
