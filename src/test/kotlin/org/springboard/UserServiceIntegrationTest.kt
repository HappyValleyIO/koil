package org.springboard

import org.springboard.user.UserCreationRequest
import org.springboard.user.UserCreationResult
import org.springboard.user.UserService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.AssertionErrors.assertEquals

@ExtendWith(SpringExtension::class)
@SpringBootTest
class UserServiceIntegrationTest(@Autowired val userService: UserService) {

  @Test
  internal fun givenAnExistingUserThenLoginWithCorrectCredentialsResultsInAccount() {
    val email = "stephen@getspringboard.dev"
    val password = "SomePassword456!"
    val created: UserCreationResult.CreatedUser =
      userService.createUser(UserCreationRequest("Stephen the tester", email, password, "tester")) as UserCreationResult.CreatedUser
    val queried = userService.login(email, password)

    assertEquals("Assert logged in user is same as created user", created.account, queried)
  }
}
