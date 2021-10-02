package org.koil.user

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.koil.auth.UserAuthority
import org.koil.fixtures.AccountFixtures
import java.time.Instant
import java.util.*

class AccountTest {

    @Test
    fun `GIVEN a user without authority WHEN granting authority THEN succeed`() {
        val user = internalTestUser(authorities = listOf())

        val updated = user.grantAuthority(UserAuthority.ADMIN)
        assertThat(updated.authorities.map { it.authority }).containsExactly(UserAuthority.ADMIN)
    }

    @Test
    fun `GIVEN a user with authority WHEN granting authority THEN make no modification`() {
        val user = internalTestUser(authorities = listOf(UserAuthority.ADMIN))

        val updated = user.grantAuthority(UserAuthority.ADMIN)
        assertThat(updated).isEqualTo(user)
    }

    @Test
    fun `GIVEN a user without the ADMIN authority WHEN checking if admin THEN return false`() {
        val user = internalTestUser(listOf(UserAuthority.USER))

        assertThat(user.isAdmin()).isFalse()
    }

    @Test
    fun `GIVEN a user with the ADMIN authority WHEN checking if admin THEN return true`() {
        val user = internalTestUser(listOf(UserAuthority.ADMIN))

        assertThat(user.isAdmin()).isTrue()
    }

    @Test
    fun `GIVEN a user with multiple authorities including ADMIN WHEN checking if admin THEN return true`() {
        val user = internalTestUser(listOf(UserAuthority.USER, UserAuthority.ADMIN))

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
                password = user.password,
                notificationSettings = NotificationSettings.default
            )
        )
    }

    private fun internalTestUser(authorities: List<UserAuthority> = listOf(UserAuthority.ADMIN)): Account {
        return Account(0,
            Instant.now(),
            "Test User",
            "stepbeek",
            UUID.randomUUID(),
            "test@example.com",
            AccountFixtures.existingAccount.password,
            null,
            NotificationSettings.default,
            authorities.map { AccountAuthority(it, Instant.now()) })
    }
}
