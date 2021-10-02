package org.koil.notifications

import org.springframework.web.util.UriComponentsBuilder
import java.util.*


data class EmailDefaults(val clientPreviewText: String, val companyDetails: String, val unsubscribeLink: String)

data class NotificationAlertSuccessModel(
    val defaults: EmailDefaults,
    val title: String,
    val headline: String,
    val actionUrl: String,
    val actionText: String,
    val thankYouText: String
)

data class AccountCreationNotificationViewModel(
    val defaults: EmailDefaults,
    val subtitle: String,
    val footer: String,
    val title: String
)

data class PasswordResetViewModel(
    val defaults: EmailDefaults,
    val appName: String,
    private val baseUrl: String,
    private val code: UUID
) {
    val resetLink: String = UriComponentsBuilder.fromUriString("$baseUrl/auth/password-reset")
        .queryParam("code", code)
        .build()
        .toUriString()
}
