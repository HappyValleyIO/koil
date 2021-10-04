package org.koil.user.verification

import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.get
import java.util.*

internal class AccountVerificationControllerTest : BaseIntegrationTest() {

    @Autowired
    lateinit var accountVerificationService: AccountVerificationService

    @Test
    internal fun `GIVEN an unverified user WHEN verifying with correct code THEN redirect to dashboard with success`() {
        withTestSession { session ->
            val account = accountRepository.findByIdOrNull(session.accountId)!!
            mockMvc.get("/dashboard/account-verification") {
                with(user(session))
                param("code", account.accountVerification.verificationCode.toString())
            }.andExpect {
                status {
                    is3xxRedirection()
                }
                header {
                    string("location", "/dashboard?accountVerified=true")
                }

            }
        }
    }

    @Test
    internal fun `GIVEN a user WHEN verifying with incorrect code THEN redirect to dashboard with error`() {
        withTestSession { session ->
            mockMvc.get("/dashboard/account-verification") {
                with(user(session))
                param("code", UUID.randomUUID().toString())
            }.andExpect {
                status {
                    is3xxRedirection()
                }
                header {
                    string("location", "/dashboard?incorrectVerificationCode=true")
                }
            }
        }
    }

    @Test
    internal fun `GIVEN a user that's already verified WHEN verifying with code THEN redirect to dashboard with error`() {
        withTestSession { session ->
            val account = accountRepository.findByIdOrNull(session.accountId)!!
            val code = account.accountVerification.verificationCode

            accountVerificationService.verifyAccount(account.accountId!!, code)

            mockMvc.get("/dashboard/account-verification") {
                with(user(session))
                param("code", code.toString())
            }.andExpect {
                status {
                    is3xxRedirection()
                }
                header {
                    string("location", "/dashboard?accountAlreadyVerified=true")
                }
            }
        }
    }
}
