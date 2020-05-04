package org.springboard

import io.sentry.spring.SentryExceptionResolver
import io.sentry.spring.SentryServletContextInitializer
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.springboard.notifications.LoggingMailSender
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletContextInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.servlet.HandlerExceptionResolver
import java.io.InputStream
import javax.mail.internet.MimeMessage
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
    fun sentryExceptionResolver(): HandlerExceptionResolver? {
        return SentryExceptionResolver()
    }

    @Bean
    fun sentryServletContextInitializer(): ServletContextInitializer? {
        return SentryServletContextInitializer()
    }

    @Bean
    @ConditionalOnMissingBean(JavaMailSender::class)
    fun loggingMailSender(): JavaMailSender {
        return LoggingMailSender()
    }

    @Bean
    @Profile("dev")
    fun javaMailSender(): JavaMailSender {
        return object : JavaMailSender {
            override fun createMimeMessage(): MimeMessage {
                return JavaMailSenderImpl().createMimeMessage()
            }

            override fun createMimeMessage(contentStream: InputStream): MimeMessage {
                return JavaMailSenderImpl().createMimeMessage(contentStream)
            }

            override fun send(mimeMessage: MimeMessage) {}

            override fun send(vararg mimeMessages: MimeMessage?) {}

            override fun send(mimeMessagePreparator: MimeMessagePreparator) {}

            override fun send(vararg mimeMessagePreparators: MimeMessagePreparator?) {}

            override fun send(simpleMessage: SimpleMailMessage) {}

            override fun send(vararg simpleMessages: SimpleMailMessage?) {}
        }
    }
}
