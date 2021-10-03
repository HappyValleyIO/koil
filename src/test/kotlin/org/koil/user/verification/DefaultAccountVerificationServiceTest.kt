package org.koil.user.verification

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koil.BaseIntegrationTest
import org.koil.extensions.isSuccess
import org.koil.user.NoAccountFoundUnexpectedlyException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import java.util.*
import kotlin.streams.toList

@RecordApplicationEvents
internal class DefaultAccountVerificationServiceTest : BaseIntegrationTest() {

    @Autowired
    lateinit var accountVerificationService: AccountVerificationService

    @Autowired
    lateinit var applicationEvents: ApplicationEvents

    @Test
    internal fun `GIVEN an account that needs verified WHEN verified with correct code THEN persist verification`() {
        withTestAccount { account ->
            val result = accountVerificationService.verifyAccount(
                account.accountId!!,
                account.accountVerification.verificationCode
            )

            assertTrue(result.isSuccess())

            val updated = accountRepository.findByIdOrNull(account.accountId!!)!!

            assertTrue(updated.isVerified())

            val event = applicationEvents.stream().toList().mapNotNull {
                it as? AccountVerifiedEvent
            }.first()

            assertThat(event.account).isEqualTo(updated)
        }
    }

    @Test
    internal fun `GIVEN no account WHEN attempting to verify THEN throw exception`() {
        assertThrows<NoAccountFoundUnexpectedlyException> {
            accountVerificationService.verifyAccount(Long.MAX_VALUE, UUID.randomUUID())
        }
    }
}
