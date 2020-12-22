package org.koil

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.core.task.TaskExecutor

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
}
