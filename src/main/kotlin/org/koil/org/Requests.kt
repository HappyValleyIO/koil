package org.koil.org

import org.koil.auth.UserAuthority
import org.koil.user.Account
import org.koil.user.UserCreationRequest
import org.koil.user.UserCreationResult
import org.koil.user.password.HashedPassword
import java.time.Instant
import java.util.*

data class OrganizationSetupRequest(
    val organizationName: String,
    val fullName: String,
    val email: String,
    val password: HashedPassword,
    val handle: String,
    val authorities: List<UserAuthority> = listOf(UserAuthority.ORG_OWNER)

) {
    fun toOrganization(): Organization {
        return Organization(
            organizationId = null,
            organizationName = organizationName,
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

sealed class OrganizationCreatedResult {
    data class CreatedOrganization(val organization: Organization, val adminAccount: Account) : OrganizationCreatedResult()
    data class UserCreationFailed(val userCreationResult: UserCreationResult) : OrganizationCreatedResult()
    object CreationFailed: OrganizationCreatedResult()
}
