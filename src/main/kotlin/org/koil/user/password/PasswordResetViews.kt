package org.koil.user.password

import org.koil.view.ViewRenderer
import java.util.*

data class PasswordResetRequestModel(
    val email: String = "",
    val emailNotFound: Boolean = false
)

data class ResetPasswordViewModel(
    val code: UUID,
    val badCredentials: Boolean = false
)

sealed class PasswordResetViews<T>(override val template: String) : ViewRenderer<T> {
    object PasswordResetRequest : PasswordResetViews<PasswordResetRequestModel>("pages/request-password-reset")
    object PasswordResetRequestCompleted : PasswordResetViews<Unit>("pages/request-password-reset-completed")
    object ResetPassword : PasswordResetViews<ResetPasswordViewModel>("pages/password-reset")
}

