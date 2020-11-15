package org.springboard

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.springboard.auth.AuthRole
import org.springboard.notifications.LoggingMailSender
import org.springboard.user.UserServiceImpl
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter
import javax.sql.DataSource

@SpringBootApplication
class SpringboardApplication

fun main(args: Array<String>) {
    runApplication<SpringboardApplication>(*args)
}

@Configuration
@EnableAsync
class BeanConfig {
    @Bean
    fun jdbi(ds: DataSource): Jdbi {
        val jdbi = Jdbi.create(ds)
        jdbi.installPlugin(KotlinPlugin())
        jdbi.installPlugin(KotlinSqlObjectPlugin())
        jdbi.installPlugin(SqlObjectPlugin())
        return jdbi
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    @ConditionalOnMissingBean(JavaMailSender::class)
    fun loggingMailSender(): JavaMailSender {
        return LoggingMailSender()
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
