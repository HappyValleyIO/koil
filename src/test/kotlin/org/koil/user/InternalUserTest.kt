package org.koil.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.koil.auth.AuthAuthority
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

        assertThat(user).isEqualTo(
            Account(
                accountId = user.accountId,
                fullName = user.fullName,
                emailAddress = user.emailAddress,
                handle = user.handle,
                publicAccountId = user.publicAccountId,
                startDate = user.startDate,
                stopDate = user.stopDate,
                authorities = user.authorities,
                password = user.password
            )
        )
    }

    private fun internalTestUser(authorities: List<AuthAuthority> = listOf(AuthAuthority.ADMIN)): Account {
        return Account(0, Instant.now(), "Test User", "stepbeek", UUID.randomUUID(), "test@example.com",
            "SomePAss123!", null, authorities.map { AccountAuthority(it, Instant.now()) })
    }
}
