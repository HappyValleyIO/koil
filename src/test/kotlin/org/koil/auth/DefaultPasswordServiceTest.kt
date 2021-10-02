package org.koil.auth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.koil.user.HashedPassword
import org.koil.user.password.PasswordResetRequestResult
import org.koil.user.password.PasswordService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

internal class DefaultPasswordServiceTest : BaseIntegrationTest() {

    @Autowired
    lateinit var passwordService: PasswordService

    @Test
    fun `GIVEN an existing account WHEN requesting a password reset THEN save a reset code and trigger an event`() {
        withTestAccount { account ->
            assertThat(account.accountPasswordReset).isNull()

            val result = passwordService.requestPasswordReset(account.emailAddress)
            assertThat(result).isEqualTo(PasswordResetRequestResult.Success)

            val updated = accountRepository.findByIdOrNull(account.accountId!!)!!
            assertThat(updated.accountPasswordReset).isNotNull()
        }
    }

    @Test
    fun `GIVEN an existing account with password reset request WHEN changing password with correct code THEN update password and delete code`() {
        withTestAccount { account ->
            passwordService.requestPasswordReset(account.emailAddress)

            val updated = accountRepository.findByIdOrNull(account.accountId!!)!!
            val newPass = HashedPassword.encode("1.SomethinngALittleDifferent!")
            passwordService.resetPassword(updated.accountPasswordReset!!.resetCode, account.emailAddress, newPass)

            val withResetPass = accountRepository.findByIdOrNull(account.accountId!!)!!

            assertThat(withResetPass.password).isEqualTo(newPass)
            assertThat(withResetPass.accountPasswordReset).isNull()
        }
    }
}
