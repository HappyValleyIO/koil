package org.springboard

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.springboard.notifications.LoggingMailSender
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
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
}
