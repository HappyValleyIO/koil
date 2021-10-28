package org.koil.user

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.koil.auth.UserAuthority
import org.koil.extensions.getOrThrow
import org.koil.extensions.toFailure
import org.koil.fixtures.AccountFixtures
import org.koil.user.verification.AccountVerification
import org.koil.user.verification.AccountVerificationViolations
import java.time.Instant
import java.util.*

class AccountTest {

    @Test
    internal fun `GIVEN an unverified account WHEN verifying with correct code THEN account is verified`() {
        val user = internalTestUser()

        assertFalse(user.isVerified())

        val code = user.accountVerification.verificationCode

        val verified = user.verifyAccount(code)
        assertTrue(verified.getOrThrow().isVerified())
    }

    @Test
    internal fun `GIVEN an unverified account WHEN verifying with incorrect code THEN account is not verified`() {
        val user = internalTestUser()

        val result = user.verifyAccount(UUID.randomUUID())
        assertThat(result).isEqualTo(AccountVerificationViolations.IncorrectCode.toFailure())
    }

    @Test
    internal fun `GIVEN a verified account WHEN verifying with correct code THEN return appropriate failure`() {
        val user = internalTestUser()
        val code = user.accountVerification.verificationCode
        val verified = user.verifyAccount(code).getOrThrow()

        val attempt = verified.verifyAccount(code)

        assertThat(attempt).isEqualTo(AccountVerificationViolations.AccountAlreadyVerified.toFailure())
    }

    @Test
    internal fun `GIVEN a verified account WHEN the email is updated THEN account need verified again`() {
        val user = internalTestUser()
        val code = user.accountVerification.verificationCode
        val verified = user.verifyAccount(code).getOrThrow()

        val updated = verified.updateEmail("updated${user.emailAddress}")

        assertFalse(updated.isVerified())
    }

    @Test
    internal fun `GIVEN a verified account WHEN the email is updated with existing address THEN make no changes`() {
        val user = internalTestUser()
        val code = user.accountVerification.verificationCode
        val verified = user.verifyAccount(code).getOrThrow()

        val updated = verified.updateEmail(user.emailAddress)

        assertTrue(updated.isVerified())
    }

    @Test
    fun `GIVEN a user without authority WHEN granting authority THEN succeed`() {
        val user = internalTestUser(authorities = listOf())

        val updated = user.grantAuthority(UserAuthority.COMPANY_OWNER)
        assertThat(updated.authorities.map { it.authority }).containsExactly(UserAuthority.COMPANY_OWNER)
    }

    @Test
    fun `GIVEN a user with authority WHEN granting authority THEN make no modification`() {
        val user = internalTestUser(authorities = listOf(UserAuthority.COMPANY_OWNER))

        val updated = user.grantAuthority(UserAuthority.COMPANY_OWNER)
        assertThat(updated).isEqualTo(user)
    }

    @Test
    fun `GIVEN a user without the ADMIN authority WHEN checking if admin THEN return false`() {
        val user = internalTestUser(listOf(UserAuthority.USER))

        assertThat(user.isCompanyOwner()).isFalse()
    }

    @Test
    fun `GIVEN a user with the ADMIN authority WHEN checking if admin THEN return true`() {
        val user = internalTestUser(listOf(UserAuthority.COMPANY_OWNER))

        assertThat(user.isCompanyOwner()).isTrue()
    }

    @Test
    fun `GIVEN a user with multiple authorities including ADMIN WHEN checking if admin THEN return true`() {
        val user = internalTestUser(listOf(UserAuthority.USER, UserAuthority.COMPANY_OWNER))

        assertThat(user.isCompanyOwner()).isTrue()
    }

    @Test
    fun `GIVEN a test user WHEN retrieving account THEN ensure fields correctly set`() {
        val user = internalTestUser()

        assertThat(user).isEqualTo(
            Account(
                companyId = 0,
                accountId = user.accountId,
                fullName = user.fullName,
                emailAddress = user.emailAddress,
                handle = user.handle,
                publicAccountId = user.publicAccountId,
                startDate = user.startDate,
                stopDate = user.stopDate,
                authorities = user.authorities,
                password = user.password,
                notificationSettings = NotificationSettings.default,
                accountVerification = user.accountVerification
            )
        )
    }

    private fun internalTestUser(authorities: List<UserAuthority> = listOf(UserAuthority.COMPANY_OWNER)): Account {
        return Account(
            0,
            0,
            Instant.now(),
            "Test User",
            "stepbeek",
            UUID.randomUUID(),
            "test@example.com",
            AccountFixtures.existingAccount.password,
            NotificationSettings.default,
            AccountVerification.create(),
            authorities.map { AccountAuthority(it, Instant.now()) },
        )
    }
}
