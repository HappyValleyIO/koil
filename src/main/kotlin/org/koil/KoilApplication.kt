package org.koil

import org.koil.auth.AuthRole
import org.koil.user.UserServiceImpl
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter

@SpringBootApplication
class KoilApplication

fun main(args: Array<String>) {
    runApplication<KoilApplication>(*args)
}

@Configuration
@EnableAsync
class BeanConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun switchUserFilter(userDetailsService: UserServiceImpl): SwitchUserFilter {
        return SwitchUserFilter().apply {
            setUserDetailsService(userDetailsService)
            setTargetUrl("/dashboard")
            setSwitchUserUrl("/admin/impersonation")
            setExitUserUrl("/admin/impersonation/logout")
            setSwitchAuthorityRole(AuthRole.ADMIN_IMPERSONATING_USER.name)
        }
    }
}
