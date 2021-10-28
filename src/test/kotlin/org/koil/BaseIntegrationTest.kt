package org.koil

import org.junit.jupiter.api.extension.ExtendWith
import org.koil.auth.DefaultUserDetailsService
import org.koil.auth.EnrichedUserDetails
import org.koil.auth.UserAuthority
import org.koil.company.CompanyService
import org.koil.company.CompanySetupRequest
import org.koil.user.*
import org.koil.user.password.HashedPassword
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import java.util.*

@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@ActiveProfiles("test")
abstract class BaseIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userService: UserServiceImpl


    @Autowired
    lateinit var userDetailsService: DefaultUserDetailsService

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var companyService: CompanyService

    protected fun withTestSession(
        email: String = "test+${Random().nextInt()}@getkoil.dev",
        password: String = "TestPass123!",
        authorities: List<UserAuthority> = listOf(UserAuthority.USER),
        foo: (EnrichedUserDetails) -> Unit
    ) {
        val id = "${Random().nextInt()}00000000".substring(0..8)
        companyService.setupCompany(CompanySetupRequest(companyName = "Company$id",
            fullName = "Test User [$id]",
            email = email,
            password = HashedPassword.encode(password),
            authorities = authorities,
            handle = "user$id"
        ))
        userDetailsService.loadUserByUsername(email).run(foo)
    }

    protected fun withTestAccount(
        email: String = "test+${Random().nextInt().toString().substring(0..4)}@getkoil.dev",
        password: String = "TestPass123!",
        authorities: List<UserAuthority> = listOf(UserAuthority.USER),
        foo: (Account) -> Unit
    ) {
        val id = "${Random().nextInt()}00000000".substring(0..8)
        companyService.setupCompany(CompanySetupRequest(companyName = "Company$id",
            fullName = "Test User [$id]",
            email = email,
            password = HashedPassword.encode(password),
            authorities = authorities,
            handle = "user$id"
        ))
        accountRepository.findAccountByEmailAddressIgnoreCase(email)!!.run(foo)
    }
}
