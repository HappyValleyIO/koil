package org.koil.company

import org.koil.auth.UserAuthority
import org.koil.user.Account
import org.koil.user.UserCreationRequest
import org.koil.user.UserCreationResult
import org.koil.user.password.HashedPassword
import java.time.Instant
import java.util.*

data class CompanySetupRequest(
    val companyName: String,
    val fullName: String,
    val email: String,
    val password: HashedPassword,
    val handle: String,
    val authorities: List<UserAuthority> = listOf(UserAuthority.COMPANY_OWNER)

) {
    fun toCompany(): Company {
        return Company(
            companyId = null,
            companyName = companyName,
            startDate = Instant.now(),
            stopDate = null,
            signupLink = UUID.randomUUID()
        )
    }

    fun toUserCreationRequest(signupLink: UUID): UserCreationRequest = UserCreationRequest(
        signupLink = signupLink,
        fullName = fullName,
        email = email,
        handle = handle,
        password = password,
        authorities = authorities
    )
}

sealed class CompanyCreationResult {
    data class CreatedCompany(val company: Company, val adminAccount: Account) : CompanyCreationResult()
    data class UserCreationFailed(val userCreationResult: UserCreationResult) : CompanyCreationResult()
    object CreationFailed: CompanyCreationResult()
}
