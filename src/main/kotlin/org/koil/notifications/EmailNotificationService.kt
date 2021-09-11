package org.koil.notifications

import org.koil.user.Account
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import java.util.*

interface NotificationService {
    fun sendAccountCreationConfirmation(account: Account)
    fun sendPasswordResetEmail(email: String, code: UUID)
}

@Component
class EmailNotificationService(
    private val sender: JavaMailSender,
    private val views: PebbleEmailViews,
    @Value("\${mail.fromAddress}") private val fromAddress: String,
    @Value("\${mail.base-url}") private val baseUrl: String
) : NotificationService {
    override fun sendAccountCreationConfirmation(account: Account) {
        val model = AccountCreationNotificationViewModel(
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

    override fun sendPasswordResetEmail(email: String, code: UUID) {
        val model = PasswordResetViewModel(
            defaults = EmailDefaults(
                "Password reset link",
                "Happy Valley IO Ltd, 2 Melville Street, Falkirk, FK1 1HZ",
                "%unsubscribe_link%"
            ),
            baseUrl = baseUrl,
            code = code,
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
