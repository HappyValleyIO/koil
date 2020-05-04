package org.springboard.dev

import org.springboard.notifications.AccountCreationNotificationModel
import org.springboard.notifications.EmailDefaults
import org.springboard.notifications.NotificationAlertSuccessModel
import org.springboard.notifications.PasswordResetModel
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@Profile("dev")
@RequestMapping("dev/email")
class EmailDevController {

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
                        "model" to AccountCreationNotificationModel(
                                defaults = EmailDefaults(
                                        "A new post for you to read",
                                        "Happy Valley IO Ltd, 2 Melville Street, Falkirk, FK1 1HZ",
                                        "%unsubscribe_link%"
                                ),
                                title = "Welcome to Springboard!",
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
                        "model" to PasswordResetModel(
                                defaults = EmailDefaults(
                                        "Password reset link",
                                        "Happy Valley IO Ltd, 2 Melville Street, Falkirk, FK1 1HZ",
                                        "%unsubscribe_link%"
                                ),
                                resetLink = "http://localhost:8080/auth/login",
                                appName = "Springboard"
                        )
                )
        )
    }
}
