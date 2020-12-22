package org.koil

import org.junit.jupiter.api.extension.ExtendWith
import org.koil.auth.AuthAuthority
import org.koil.user.EnrichedUserDetails
import org.koil.user.UserCreationRequest
import org.koil.user.UserCreationResult
import org.koil.user.UserServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import java.util.*

@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class BaseIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userService: UserServiceImpl

    protected fun withTestUser(email: String = "test+${Random().nextInt()}@getkoil.dev",
                               password: String = "TestPass123!",
                               authorities: List<AuthAuthority> = listOf(AuthAuthority.USER),
                               foo: (EnrichedUserDetails) -> Unit
    ) {
        val id = Random().nextInt().toString().substring(0..8)
        val request = UserCreationRequest(
                fullName = "Test User [$id]",
                email = email,
                password = password,
                authorities = authorities,
                handle = "user$id"
        )

        (userService.createUser(request) as UserCreationResult.CreatedUser)
        userService.loadUserByUsername(email)!!.run(foo)
    }
}