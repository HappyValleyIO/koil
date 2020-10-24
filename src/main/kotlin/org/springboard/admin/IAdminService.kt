package org.springboard.admin

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springboard.auth.AuthAuthority
import org.springboard.user.UserCreationRequest
import org.springboard.user.UserCreationResult
import org.springboard.user.UserService
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

interface IAdminService {
    fun createAdminFromEmail(adminEmail: String): UserCreationResult
}

@Component
class AdminServiceImpl(private val userService: UserService,
                       @Value("\${admin-user.email:}") private val adminEmailFromEnv: String?) : IAdminService, InitializingBean {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(AdminServiceImpl::class.java)
    }

    override fun afterPropertiesSet() {
        adminEmailFromEnv?.takeIf { it.trim() != "" }?.let { email -> createAdminFromEmail(email) }
    }

    override fun createAdminFromEmail(adminEmail: String): UserCreationResult {
        return userService.createUser(UserCreationRequest("Default Admin",
                adminEmail,
                "RandomPass!${Random().nextInt()}",
                "DefaultAdmin",
                listOf(AuthAuthority.ADMIN))).also {
            if (it is UserCreationResult.CreatedUser) {
                LOGGER.info("Created an admin account with email {}", adminEmail)
            }
        }
    }

}
