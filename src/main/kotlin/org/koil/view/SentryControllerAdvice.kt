package org.koil.view

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class SentryControllerAdvice(
    @Value("\${sentry.dsn:#{null}}") private val sentryDsn: String?,
    @Value("\${sentry.release:#{null}}") private val sentryRelease: String?,
) {
    @ModelAttribute("sentry_dsn")
    fun sentryDsn(): String? = sentryDsn

    @ModelAttribute("sentry_release")
    fun sentryRelease(): String? = sentryRelease?.let { "koil@$it" }
}
