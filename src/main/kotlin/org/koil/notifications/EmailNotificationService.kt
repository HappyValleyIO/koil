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
    fun sendAccountUpdateConfirmation(account: Account)
}

@Component
class EmailNotificationService(
    private val sender: JavaMailSender,
    private val views: PebbleEmailViews,
    @Value("\${mail.fromAddress}") private val fromAddress: String,
    @Value("\${mail.base-url}") private val baseUrl: String
) : NotificationService {
    override fun sendAccountCreationConfirmation(account: Account) {
        val model = NotificationAlertSuccessModel(
            defaults = EmailDefaults(
                "Welcome to koil!",
                "Happy Valley IO Ltd, 2 Melville Street, Falkirk, FK1 1HZ",
                "%unsubscribe_link%"
            ),
            title = "Welcome to koil.",
            headline = "We're very happy to have you.",
            actionUrl = "$baseUrl/dashboard/account-verification?code=${account.accountVerification.verificationCode}",
            actionText = "Verify your account",
            thankYouText = "Thank you for signing up!"
        )

        sendMessage(views.renderAlertSuccess(model), account.emailAddress, model.title)
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

    override fun sendAccountUpdateConfirmation(account: Account) {
        val model = NotificationAlertSuccessModel(
            defaults = EmailDefaults(
                "Koil Account Updated.",
                "Happy Valley IO Ltd, 2 Melville Street, Falkirk, FK1 1HZ",
                "%unsubscribe_link%"
            ),
            title = "You've updated your account.",
            headline = "If you're confused about why you've received this email then we recommend updating your password.",
            actionUrl = "$baseUrl/dashboard/account-verification?code=${account.accountVerification.verificationCode}",
            actionText = "Verify your account",
            thankYouText = "Thank you for using koil!"
        )

        sendMessage(views.renderAlertSuccess(model), account.emailAddress, model.title)
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
