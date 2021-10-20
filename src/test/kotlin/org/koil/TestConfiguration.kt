package org.koil

import org.koil.dev.LoggingMailSender
import org.koil.fixtures.InMemoryImageStorage
import org.koil.image.Storage
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import org.springframework.mail.javamail.JavaMailSender

@Configuration
class TestConfiguration {

    /**
     * We have a synchronous event executor so that we can ensure that all async events happen before the test runs finish.
     */
    @Bean
    @Primary
    fun taskExecutor(): TaskExecutor? {
        return SyncTaskExecutor()
    }

    @Bean
    @ConditionalOnMissingBean(JavaMailSender::class)
    fun loggingMailSender(): JavaMailSender {
        return LoggingMailSender()
    }

    @Bean
    @Profile("!dev")
    fun inMemoryStorage(): Storage = InMemoryImageStorage()
}
