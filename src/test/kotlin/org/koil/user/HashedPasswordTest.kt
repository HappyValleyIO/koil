package org.koil.user

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koil.user.password.HashedPassword

internal class HashedPasswordTest {
    @Test
    internal fun `un-encoded password cannot be wrapped`() {
        assertThrows<IllegalArgumentException> {
            HashedPassword("Unencoded!")
        }
    }
}
