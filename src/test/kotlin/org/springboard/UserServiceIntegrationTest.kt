package org.springboard

import org.assertj.core.internal.bytebuddy.utility.RandomString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springboard.auth.AuthAuthority
import org.springboard.user.EnrichedUserDetails
import org.springboard.user.UserCreationRequest
import org.springboard.user.UserCreationResult
import org.springboard.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.AssertionErrors.assertEquals

@ExtendWith(SpringExtension::class)
@SpringBootTest
class UserServiceIntegrationTest(@Autowired val userService: UserService, @Autowired val userDetails: UserDetailsService) {

    @Test
    fun `GIVEN an existing user WHEN logging in with correct credentials THE return a user result`() {
        val email = "stephen+${RandomString.make()}@getspringboard.dev"
        val password = "SomePassword456!"
        val created: UserCreationResult.CreatedUser =
                userService.createUser(UserCreationRequest("Stephen the tester", email, password, "tester", listOf(AuthAuthority.ADMIN))) as UserCreationResult.CreatedUser
        val queried = userDetails.loadUserByUsername(email) as EnrichedUserDetails

        assertEquals("Assert logged in user is same as created user", created.account.accountId, queried.accountId)
        assertEquals("Assert logged in user is same as created user", created.account.handle, queried.handle)
        assertEquals("Assert logged in user is same as created user", created.account.email, queried.username)
    }

    @Test
    fun `GIVEN no user for principal WHEN creating a new user THEN ensure they only have USER authority by default`() {
        val email = "stephen+${RandomString.make()}@getspringboard.dev"
        val password = "SomePassword456!"
        userService.createUser(UserCreationRequest("Stephen the tester", email, password, "tester")) as UserCreationResult.CreatedUser
        val queried = userDetails.loadUserByUsername(email) as EnrichedUserDetails

        assertEquals("Only USER authority present", listOf("USER"), queried.authorities.map { it.authority })
    }

    @Test
    fun `GIVEN no user for principal WHEN creating a new user with admin THEN ensure they have USER and ADMIN rights authorities`() {
        val email = "stephen+${RandomString.make()}@getspringboard.dev"
        val password = "SomePassword456!"
        userService.createUser(UserCreationRequest("Stephen the tester", email, password, "tester", listOf(AuthAuthority.ADMIN, AuthAuthority.USER))) as UserCreationResult.CreatedUser
        val queried = userDetails.loadUserByUsername(email) as EnrichedUserDetails

        assertEquals("USER and ADMIN authorities present", listOf("USER", "ADMIN").sorted(), queried.authorities.map { it.authority }.sorted())
    }
}
