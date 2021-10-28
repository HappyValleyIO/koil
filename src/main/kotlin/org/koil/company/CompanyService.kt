package org.koil.company

import org.koil.admin.AdminServiceImpl
import org.koil.user.UserCreationResult
import org.koil.user.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface CompanyService {
    fun setupCompany(request: CompanySetupRequest): CompanyCreationResult
}

@Component
class CompanyServiceImpl(
    private val repository: CompanyRepository,
    private val userService: UserService
) : CompanyService {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(AdminServiceImpl::class.java)
    }

    override fun setupCompany(request: CompanySetupRequest): CompanyCreationResult {
        return try {
            createCompanyAndUser(request)
        } catch (exception: Exception) {
            LOGGER.error("Failed to setup company and admin user with exception [$exception]", exception)
            CompanyCreationResult.CreationFailed
        }
    }

    @Transactional
    fun createCompanyAndUser(request: CompanySetupRequest): CompanyCreationResult {
        val company = repository.save(request.toCompany())
        val adminUserCreationResult = userService.createUser(request.toUserCreationRequest(company.signupLink))

        return when (adminUserCreationResult) {
            is UserCreationResult.CreatedUser -> CompanyCreationResult.CreatedCompany(
                company,
                adminUserCreationResult.account
            )
            UserCreationResult.InvalidSignupLink -> CompanyCreationResult.UserCreationFailed(adminUserCreationResult)
            UserCreationResult.UserAlreadyExists -> CompanyCreationResult.UserCreationFailed(adminUserCreationResult)
        }
    }

}
