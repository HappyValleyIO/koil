package org.koil.auth

import org.hibernate.validator.constraints.Length
import org.koil.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.Pattern

data class PasswordResetAttempt(
    @get:Pattern(
        regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
        message = "Code given is not a valid reset code"
    ) val code: String,
    @get:Email(message = "Must be a valid email address") val email: String?,
    @get:Length(min = 8, message = "Password must be at least 8 characters long") val password: String?,
    val passwordConfirm: String?
) {
    val passwordsMatch: Boolean = password == passwordConfirm

    fun parsedCode(): UUID = UUID.fromString(code)
}

data class PasswordResetRequest(@get:Email(message = "Must be a valid email address") val email: String)

@Controller
@RequestMapping("/auth")
class PasswordResetController(
    @Autowired val users: UserService,
    @Autowired val auth: AuthService,
) {

    @GetMapping("/request-password-reset")
    fun requestPasswordReset(): ModelAndView {
        return AuthViews.PasswordResetRequest.render(PasswordResetRequestModel())
    }

    @PostMapping("/request-password-reset")
    fun requestPasswordReset(
        @Valid form: PasswordResetRequest,
        result: BindingResult
    ): ModelAndView {
        val model = PasswordResetRequestModel(form, result.errors().toMutableMap(), false)

        return if (result.hasErrors()) {
            AuthViews.PasswordResetRequest.render(model, HttpStatus.BAD_REQUEST)
        } else {
            return when (auth.requestPasswordReset(email = form.email)) {
                is PasswordResetRequestResult.Success -> AuthViews.PasswordResetRequest.render(model.copy(completed = true))
                is PasswordResetRequestResult.CouldNotFindUserWithEmail -> AuthViews.PasswordResetRequest.render(
                    model.copy(
                        completed = true
                    ),
                    httpStatus = HttpStatus.BAD_REQUEST
                )
                is PasswordResetRequestResult.FailedUnexpectedly -> {
                    model.errors["unexpected"] = """
                      An unexpected error has occurred - please try again. If you continue to experience the issue then please get in touch at support@getkoil.dev
                      """.trimIndent()

                    AuthViews.PasswordResetRequest.render(
                        model.copy(completed = true),
                        HttpStatus.INTERNAL_SERVER_ERROR
                    )
                }
            }
        }
    }

    @GetMapping("/password-reset")
    fun resetPasswordPage(@RequestParam("code") code: UUID): ModelAndView {
        val model = ResetPasswordViewModel.fromCode(code)
        return AuthViews.ResetPassword.render(model)
    }

    @PostMapping("/password-reset")
    fun resetPassword(
        request: HttpServletRequest,
        @Valid attempt: PasswordResetAttempt,
        result: BindingResult
    ): ModelAndView {
        if (!attempt.passwordsMatch) {
            result.addError(FieldError(result.objectName, "passwordConfirm", "Passwords don't match!"))
        }

        return if (result.hasErrors()) {
            AuthViews.ResetPassword.render(ResetPasswordViewModel(), httpStatus = HttpStatus.BAD_REQUEST)
        } else {
            auth.resetPassword(attempt.parsedCode(), attempt.email ?: "", attempt.password ?: "")
            request.login(attempt.email, attempt.password)
            ModelAndView("redirect:/dashboard")
        }
    }

}

fun BindingResult.errors(): Map<String, String?> {
    return this.fieldErrors.associate { it.field to it.defaultMessage }
}
