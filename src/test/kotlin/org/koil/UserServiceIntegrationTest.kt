package org.koil

import org.assertj.core.internal.bytebuddy.utility.RandomString
import org.junit.jupiter.api.Test
import org.koil.auth.AuthAuthority
import org.koil.user.EnrichedUserDetails
import org.koil.user.UserCreationRequest
import org.koil.user.UserCreationResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.util.AssertionErrors.assertEquals
import org.springframework.test.util.AssertionErrors.assertTrue

class UserServiceIntegrationTest(@Autowired val userDetails: UserDetailsService) : BaseIntegrationTest() {

    @Test
    fun `GIVEN an existing user WHEN logging in with correct credentials THE return a user result`() {
        val email = "stephen+${RandomString.make()}@getkoil.dev"
        val password = "SomePassword456!"
        val created: UserCreationResult.CreatedUser =
            userService.createUser(
                UserCreationRequest(
                    "Stephen the tester",
                    email,
                    password,
                    "tester",
                    listOf(AuthAuthority.ADMIN)
                )
            ) as UserCreationResult.CreatedUser
        val queried = userDetails.loadUserByUsername(email) as EnrichedUserDetails

        assertEquals("Assert logged in user is same as created user", created.account.accountId, queried.accountId)
        assertEquals("Assert logged in user is same as created user", created.account.handle, queried.handle)
        assertEquals("Assert logged in user is same as created user", created.account.emailAddress, queried.username)
    }

    @Test
    fun `GIVEN no user for principal WHEN creating a new user THEN ensure they only have USER authority by default`() {
        val email = "stephen+${RandomString.make()}@getkoil.dev"
        val password = "SomePassword456!"
        userService.createUser(
            UserCreationRequest(
                "Stephen the tester",
                email,
                password,
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
                password,
                "tester",
                listOf(AuthAuthority.ADMIN, AuthAuthority.USER)
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

        val existingUser: UserCreationResult = userService.createUser(
            UserCreationRequest(
                "Mr. Existing",
                "existing$slug",
                "password1!",
                "existing$slug"
            )
        ) as UserCreationResult.CreatedUser

        val attemptedEmailSteal = userService.createUser(
            UserCreationRequest(
                "Mr. Existing",
                "existing$slug",
                "password1!",
                "existing$slug"
            )
        ) as UserCreationResult.UserAlreadyExists

        val attemptedEmailStealCapitalized = userService.createUser(
            UserCreationRequest(
                "Mr. Existing",
                "Existing$slug",
                "password1!",
                "existing$slug"
            )
        ) as UserCreationResult.UserAlreadyExists
    }
}
