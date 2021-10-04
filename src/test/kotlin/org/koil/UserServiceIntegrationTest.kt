package org.koil

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.assertj.core.internal.bytebuddy.utility.RandomString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koil.auth.EnrichedUserDetails
import org.koil.auth.UserAuthority
import org.koil.user.*
import org.koil.user.password.HashedPassword
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import org.springframework.test.util.AssertionErrors.assertEquals
import kotlin.streams.toList

@RecordApplicationEvents
class UserServiceIntegrationTest(@Autowired val userDetails: UserDetailsService) : BaseIntegrationTest() {

    @Autowired
    lateinit var applicationEvents: ApplicationEvents

    @Test
    fun `GIVEN an existing user WHEN logging in with correct credentials THE return a user result`() {
        val email = "stephen+${RandomString.make()}@getkoil.dev"
        val password = "SomePassword456!"
        val created: UserCreationResult.CreatedUser =
            userService.createUser(
                UserCreationRequest(
                    "Stephen the tester",
                    email,
                    password = HashedPassword.encode(password),
                    "tester",
                    listOf(UserAuthority.ADMIN)
                )
            ) as UserCreationResult.CreatedUser
        val queried = userDetails.loadUserByUsername(email) as EnrichedUserDetails

        assertThat(queried.accountId).isEqualTo(created.account.accountId)
        assertThat(queried.handle).isEqualTo(created.account.handle)
        assertThat(queried.username).isEqualTo(created.account.emailAddress)
    }

    @Test
    fun `GIVEN no user for principal WHEN creating a new user THEN ensure they only have USER authority by default`() {
        val email = "stephen+${RandomString.make()}@getkoil.dev"
        val password = "SomePassword456!"
        userService.createUser(
            UserCreationRequest(
                "Stephen the tester",
                email,
                password = HashedPassword.encode(password),
                "tester"
            )
        ) as UserCreationResult.CreatedUser
        val queried = userDetails.loadUserByUsername(email) as EnrichedUserDetails

        assertEquals("Only USER authority present", listOf("USER"), queried.authorities.map { it.authority })
    }

    @Test
    fun `GIVEN no user for principal WHEN creating a new user with admin THEN ensure they have USER and ADMIN rights authorities`() {
        val email = "stephen+${RandomString.make()}@getkoil.dev"
        val password = "SomePassword456!"
        userService.createUser(
            UserCreationRequest(
                "Stephen the tester",
                email,
                password = HashedPassword.encode(password),
                "tester",
                listOf(UserAuthority.ADMIN, UserAuthority.USER)
            )
        ) as UserCreationResult.CreatedUser
        val queried = userDetails.loadUserByUsername(email) as EnrichedUserDetails

        assertEquals(
            "USER and ADMIN authorities present",
            listOf("USER", "ADMIN").sorted(),
            queried.authorities.map { it.authority }.sorted()
        )
    }

    @Test
    fun `GIVEN user with chosen email already exists WHEN create user THEN do not allow use of this email`() {
        val slug = RandomString.make()

        userService.createUser(
            UserCreationRequest(
                "Mr. Existing",
                "existing$slug",
                password = HashedPassword.encode("password1!"),
                "existing$slug"
            )
        ) as UserCreationResult.CreatedUser

        userService.createUser(
            UserCreationRequest(
                "Mr. Existing",
                "existing$slug",
                password = HashedPassword.encode("password1!"),
                "existing$slug"
            )
        ) as UserCreationResult.UserAlreadyExists

        userService.createUser(
            UserCreationRequest(
                "Mr. Existing",
                "Existing$slug",
                password = HashedPassword.encode("password1!"),
                "existing$slug"
            )
        ) as UserCreationResult.UserAlreadyExists
    }

    @Test
    internal fun `GIVEN an existing user WHEN updating with unique details THEN report success`() {
        val slug = RandomString.make()

        withTestAccount { account ->
            val request = UpdateUserSettingsRequest(
                name = slug,
                email = "updated${account.emailAddress}",
                weeklySummary = !account.notificationSettings.weeklyActivity,
                updateOnAccountChange = !account.notificationSettings.emailOnAccountChange
            )

            val result = userService.updateUser(account.accountId!!, request) as AccountUpdateResult.AccountUpdated

            assertThat(result.account.fullName).isEqualTo(request.name)
            assertThat(result.account.notificationSettings.emailOnAccountChange).isEqualTo(request.updateOnAccountChange)
            assertThat(result.account.emailAddress).isEqualTo(request.normalizedEmail)
            assertThat(result.account.notificationSettings.weeklyActivity).isEqualTo(request.weeklySummary)

            val event = applicationEvents.stream().toList().mapNotNull {
                it as? AccountUpdateEvent
            }.first()

            assertThat(event.account).isEqualTo(result.account)
        }
    }

    @Test
    internal fun `GIVEN an existing user WHEN updating with already taken email THEN report success`() {
        val slug = RandomString.make()
        withTestAccount { other ->
            withTestAccount { account ->
                val request = UpdateUserSettingsRequest(
                    name = slug,
                    email = other.emailAddress,
                    weeklySummary = !account.notificationSettings.weeklyActivity,
                    updateOnAccountChange = !account.notificationSettings.emailOnAccountChange
                )

                val result = userService.updateUser(account.accountId!!, request)

                assertThat(result).isEqualTo(
                    AccountUpdateResult.EmailAlreadyInUse(
                        other.emailAddress
                    )
                )
            }
        }
    }

    @Test
    internal fun `GIVEN an existing user WHEN attempting to update a nonexistent user THEN throw exception`() {
        assertThrows<NoAccountFoundUnexpectedlyException> {
            userService.updateUser(
                accountId = Long.MAX_VALUE,
                request = UpdateUserSettingsRequest(
                    name = "Test",
                    email = "test@example.com",
                    false,
                    updateOnAccountChange = false
                )
            )
        }
    }
}
