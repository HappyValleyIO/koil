package org.springboard.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.springboard.auth.AuthAuthority
import java.time.Instant
import java.util.*

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

    private fun internalTestUser(authorities: List<AuthAuthority> = listOf(AuthAuthority.ADMIN)): InternalUser {
        return UserQueryResult(0, "test@example.com", "SomePass123!", Instant.now(),
                "Test User", "user", null, authorities, UUID.randomUUID())
    }
}
