package org.springboard.base

import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class SentryControllerAdvice {
    @ModelAttribute("sentry_dsn")
    fun sentryDsn(): String? {
        return System.getenv("SENTRY_DSN")
    }
}
