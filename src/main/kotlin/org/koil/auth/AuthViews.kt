package org.koil.auth

import org.koil.view.ViewRenderer
import java.util.*

data class LoginViewModel(
    val redirect: Boolean = false,
    val badCredentials: Boolean = false
)

data class RegistrationViewModel(
    val email: String,
    val emailAlreadyTaken: Boolean = false
)

data class PasswordResetRequestModel(
    val email: String = "",
    val emailNotFound: Boolean = false
)

data class ResetPasswordViewModel(
    val code: UUID,
    val badCredentials: Boolean = false
)

sealed class AuthViews<T>(override val template: String) : ViewRenderer<T> {
    object Login : AuthViews<LoginViewModel>("pages/login")
    object Register : AuthViews<RegistrationViewModel>("pages/register")
    object PasswordResetRequest : AuthViews<PasswordResetRequestModel>("pages/request-password-reset")
    object PasswordResetRequestCompleted : AuthViews<Unit>("pages/request-password-reset-completed")
    object ResetPassword : AuthViews<ResetPasswordViewModel>("pages/password-reset")
}

