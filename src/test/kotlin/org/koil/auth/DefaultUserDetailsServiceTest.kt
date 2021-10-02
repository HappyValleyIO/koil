package org.koil.auth

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koil.BaseIntegrationTest
import org.springframework.security.core.userdetails.UsernameNotFoundException

class DefaultUserDetailsServiceTest : BaseIntegrationTest() {

    @Test
    internal fun `GIVEN a user exists WHEN loading the user THEN return details successfully`() {
        withTestAccount { account ->
            val result = userDetailsService.loadUserByUsername(account.emailAddress)

            assertThat(result.accountId).isEqualTo(account.accountId)
            assertThat(result.handle).isEqualTo(account.handle)
            assertThat(result.username).isEqualTo(account.emailAddress)
            assertThat(result.password).isEqualTo(account.password.encodedPassword)
        }
    }

    @Test
    internal fun `GIVEN a user does not exist WHEN loading the user THEN throw an exception`() {
        assertThrows<UsernameNotFoundException> {
            userDetailsService.loadUserByUsername("nonexistent@fake.com")
        }
    }
}
