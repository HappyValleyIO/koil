package org.koil.dev

import org.koil.notifications.AccountCreationNotificationViewModel
import org.koil.notifications.EmailDefaults
import org.koil.notifications.NotificationAlertSuccessModel
import org.koil.notifications.PasswordResetViewModel
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.mail.MailSender
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import java.util.*

@Controller
@Profile("dev")
@RequestMapping("dev/email")
class EmailDevController(val loggingMailSender: MailSender) {

    @GetMapping("/alert-success")
    fun alertSuccessEmail(): ModelAndView {
        return ModelAndView(
            "email/alert-success-inlined",
            mapOf(
                "model" to NotificationAlertSuccessModel(
                    defaults = EmailDefaults(
                        "A notification for you to check out",
                        "Happy Valley IO Ltd, 2 Melville Street, Falkirk, FK1 1HZ",
                        "%unsubscribe_link%"
                    ),
                    title = "A thing happened with the other thing!",
                    headline = "Ready for more?",
                    actionUrl = "https://localhost:8080/dashboard/history",
                    actionText = "Read Now",
                    thankYouText = "Thanks for following!"
                )
            )
        )
    }

    @GetMapping("/welcome")
    fun welcomeEmail(): ModelAndView {
        return ModelAndView(
            "email/welcome-inlined",
            mapOf(
                "model" to AccountCreationNotificationViewModel(
                    defaults = EmailDefaults(
                        "A new post for you to read",
                        "Happy Valley IO Ltd, 2 Melville Street, Falkirk, FK1 1HZ",
                        "%unsubscribe_link%"
                    ),
                    title = "Welcome to koil!",
                    subtitle = "We're happy to have you!",
                    footer = "Thanks for signing up! Get in touch if you have any trouble!"
                )
            )
        )
    }

    @GetMapping("/password")
    fun passwordEmail(): ModelAndView {
        return ModelAndView(
            "email/password-inlined",
            mapOf(
                "model" to PasswordResetViewModel(
                    defaults = EmailDefaults(
                        "Password reset link",
                        "Happy Valley IO Ltd, 2 Melville Street, Falkirk, FK1 1HZ",
                        "%unsubscribe_link%"
                    ),
                    baseUrl = "http://localhost:8080/",
                    appName = "koil",
                    code = UUID.randomUUID()
                )
            )
        )
    }

    @GetMapping("/last-sent")
    fun sentEmails(@RequestParam("to") to: String?): ResponseEntity<String> {
        val matchingEmails = (loggingMailSender as LoggingMailSender).getEmails().filter { it.to == to || to == null}
        val email = if (matchingEmails.size > 1) {
            matchingEmails.last().body
        } else {
            matchingEmails.firstOrNull()?.body ?: ""
        }

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(email)
    }
}
