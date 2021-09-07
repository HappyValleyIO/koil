package org.koil.auth

import org.koil.user.Account
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import java.util.*

data class LoginViewModel(
    val email: String = "",
    val redirect: Boolean = false,
    val badCredentials: Boolean = false,
    val errors: Map<String, String?> = mapOf()
)

data class RegistrationViewModel(
    val attempt: RegistrationAttempt? = null,
    val errors: MutableMap<String, String?> = mutableMapOf(),
    val account: Account? = null
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
        fun fromCode(code: String): ResetPasswordViewModel = try {
            val parsed = UUID.fromString(code)
            ResetPasswordViewModel(code = parsed)
        } catch (e: Exception) {
            ResetPasswordViewModel(
                errors = mutableMapOf(
                    "code" to "Whoops! It looks like you've arrived at this page without a valid reset code. Please click the button in your email again."
                ),
                status = HttpStatus.BAD_REQUEST
            )
        }
    }
}

interface IAuthViews {
    fun login(model: LoginViewModel): ModelAndView

    fun register(model: RegistrationViewModel): ModelAndView

    fun requestPasswordReset(model: PasswordResetRequestModel): ModelAndView

    fun resetPassword(model: ResetPasswordViewModel): ModelAndView
}

@Component
class AuthViewsImpl : IAuthViews {
    override fun login(model: LoginViewModel): ModelAndView {
        return ModelAndView("pages/login", mapOf("model" to model))
    }

    override fun register(model: RegistrationViewModel): ModelAndView {
        return ModelAndView("pages/register", mapOf("model" to model))
    }

    override fun requestPasswordReset(model: PasswordResetRequestModel): ModelAndView {
        return ModelAndView("pages/request-password-reset", mapOf("model" to model))
    }

    override fun resetPassword(model: ResetPasswordViewModel): ModelAndView {
        return ModelAndView("pages/password-reset", mapOf("model" to model))
            .apply { status = model.status }
    }

}
