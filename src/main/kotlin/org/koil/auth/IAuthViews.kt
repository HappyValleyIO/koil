package org.koil.auth

import org.koil.user.Account
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

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
    val code: String?
)

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
    }

}
