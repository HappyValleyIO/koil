package org.springboard.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.springboard.auth.AuthAuthority
import org.springboard.internalTestUser

class InternalUserTest {

    @Test
    fun `GIVEN a user without the ADMIN authority WHEN checking if admin THEN return false`() {
        val user = internalTestUser(listOf(AuthAuthority.USER))

        assertThat(user.isAdmin()).isFalse()
    }

    @Test
    fun `GIVEN a user with the ADMIN authority WHEN checking if admin THEN return true`() {
        val user = internalTestUser(listOf(AuthAuthority.ADMIN))

        assertThat(user.isAdmin()).isTrue()
    }

    @Test
    fun `GIVEN a user with multiple authorities including ADMIN WHEN checking if admin THEN return true`() {
        val user = internalTestUser(listOf(AuthAuthority.USER, AuthAuthority.ADMIN))

        assertThat(user.isAdmin()).isTrue()
    }

    @Test
    fun `GIVEN a test user WHEN retrieving account THEN ensure fields correctly set`() {
        val user = internalTestUser()

        assertThat(user.toAccount()).isEqualTo(Account(
                accountId = user.accountId,
                fullName = user.fullName,
                email = user.email,
                handle = user.handle,
                publicId = user.publicId))
    }
}
