package org.koil.auth

import org.koil.view.ViewRenderer
import org.springframework.http.HttpStatus
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
    val attempt: PasswordResetRequest? = null,
    val errors: MutableMap<String, String?> = mutableMapOf(),
    val completed: Boolean = false
)

data class ResetPasswordViewModel(
    val attempt: PasswordResetAttempt? = null,
    val errors: MutableMap<String, String?> = mutableMapOf(),
    val code: UUID? = null,
    val status: HttpStatus = HttpStatus.OK
) {
    companion object {
        fun fromCode(code: UUID): ResetPasswordViewModel =
            ResetPasswordViewModel(code = code)
    }
}

sealed class AuthViews<T>(override val template: String) : ViewRenderer<T> {
    object Login : AuthViews<LoginViewModel>("pages/login")
    object Register : AuthViews<RegistrationViewModel>("pages/register")
    object PasswordResetRequest : AuthViews<PasswordResetRequestModel>("pages/request-password-reset")
    object ResetPassword : AuthViews<ResetPasswordViewModel>("pages/password-reset")
}

