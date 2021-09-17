package org.koil.admin.accounts

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.koil.auth.AuthAuthority
import org.koil.fixtures.AccountFixtures

internal class UpdateAccountRequestTest {
    @Test
    internal fun `updates existing account`() {
        val account = AccountFixtures.existingAccount

        val update = UpdateAccountRequest(
            "Updated Name",
            "updated${account.emailAddress}  ",
            "u${account.handle}",
            AuthAuthority.values().toList()
        )

        val result = update.update(account)

        assertThat(result.fullName).isEqualTo(update.fullName)
        assertThat(result.emailAddress).isEqualTo(update.normalizedEmail)
        assertThat(result.handle).isEqualTo(update.handle)
        assertThat(result.authorities.map { it.authority }).isEqualTo(update.authorities)
    }
}
