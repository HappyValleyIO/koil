package org.koil.auth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder

internal class AuthServiceImplTest : BaseIntegrationTest() {

    @Autowired
    lateinit var authService: AuthService

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun `GIVEN an existing account WHEN requesting a password reset THEN save a reset code and trigger an event`() {
        withTestAccount { account ->
            assertThat(account.accountPasswordReset).isNull()

            val result = authService.requestPasswordReset(account.emailAddress)
            assertThat(result).isEqualTo(PasswordResetRequestResult.Success)

            val updated = accountRepository.findByIdOrNull(account.accountId!!)!!
            assertThat(updated.accountPasswordReset).isNotNull()
        }
    }

    @Test
    fun `GIVEN an existing account with password reset request WHEN changing password with correct code THEN update password and delete code`() {
        withTestAccount { account ->
            authService.requestPasswordReset(account.emailAddress)

            val updated = accountRepository.findByIdOrNull(account.accountId!!)!!
            val newPass = "1.SomethinngALittleDifferent!"
            authService.resetPassword(updated.accountPasswordReset!!.resetCode, account.emailAddress, newPass)

            val withResetPass = accountRepository.findByIdOrNull(account.accountId!!)!!

            assertTrue(passwordEncoder.matches(newPass, withResetPass.password))
            assertThat(withResetPass.accountPasswordReset).isNull()
        }
    }
}
