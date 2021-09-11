package org.koil.notifications

import com.mitchellbosecke.pebble.PebbleEngine
import org.koil.user.Account
import org.koil.user.AccountCreationEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.StringWriter
import java.net.URLEncoder
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

data class AccountCreationNotificationModel(
    val defaults: EmailDefaults,
    val subtitle: String,
    val footer: String,
    val title: String
)

data class PasswordResetModel(val defaults: EmailDefaults, val resetLink: String, val appName: String)

@Component
class NotificationListener(val notifications: NotificationService) {
    @Async
    @EventListener
    fun onAccountCreation(event: AccountCreationEvent) {
        notifications.sendAccountCreationConfirmation(event.account)
    }
}

@Component
class NotificationService(
    private val sender: JavaMailSender,
    private val views: EmailViews,
    @Value("\${mail.fromAddress}") private val fromAddress: String
) {
    fun sendAccountCreationConfirmation(account: Account) {
        val model = AccountCreationNotificationModel(
            defaults = EmailDefaults(
                "Welcome to koil!",
                "Happy Valley IO Ltd, 2 Melville Street, Falkirk, FK1 1HZ",
                "%unsubscribe_link%"
            ),
            title = "Welcome to koil!",
            subtitle = "We're happy to have you!",
            footer = "Get in touch if you have any trouble!"
        )

        sendMessage(views.renderWelcomeMessage(model), account.emailAddress, model.title)
    }

    fun sendPasswordResetEmail(email: String, code: UUID?) {
        val url = "http://localhost:8080/auth/password-reset?code=" + URLEncoder.encode(code.toString(), "UTF-8")
        val model = PasswordResetModel(
            defaults = EmailDefaults(
                "Password reset link",
                "Happy Valley IO Ltd, 2 Melville Street, Falkirk, FK1 1HZ",
                "%unsubscribe_link%"
            ),
            resetLink = url,
            appName = "koil"
        )

        sendMessage(views.renderPasswordReset(model), email, "Password reset link")
    }

    private fun sendMessage(html: String, to: String, subject: String) {
        val message = sender.createMimeMessage()

        val helper = MimeMessageHelper(message, "utf-8")

        helper.setText(html, true)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setFrom(fromAddress)

        sender.send(message)
    }
}

@Component
class EmailViews(val pebble: PebbleEngine) {
    fun renderAlertSuccess(model: NotificationAlertSuccessModel): String {
        return pebble.renderWithModel("email/alert-success-inlined", mapOf("model" to model))
    }

    fun renderWelcomeMessage(model: AccountCreationNotificationModel): String {
        return pebble.renderWithModel("email/welcome-inlined", mapOf("model" to model))
    }

    fun renderPasswordReset(model: PasswordResetModel): String {
        return pebble.renderWithModel("email/password-inlined", mapOf("model" to model))
    }
}

fun PebbleEngine.renderWithModel(template: String, model: Map<String, Any?>): String {
    val writer = StringWriter()
    this.getTemplate(template).evaluate(writer, model)
    return writer.toString()
}
