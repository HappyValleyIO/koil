package org.koil.auth

import org.hibernate.validator.constraints.Length
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
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
    @Autowired private val auth: AuthService,
) {
    @GetMapping("/request-password-reset")
    fun requestPasswordReset(
        @RequestParam("email", defaultValue = "") email: String,
        @RequestParam("completed", defaultValue = "false") completed: Boolean
    ): ModelAndView {
        return if (completed) {
            AuthViews.PasswordResetRequestCompleted.render(Unit)
        } else {
            AuthViews.PasswordResetRequest.render(PasswordResetRequestModel(email = email))
        }
    }

    @PostMapping("/request-password-reset")
    fun requestPasswordReset(
        @Valid @ModelAttribute("submitted") submitted: PasswordResetRequest,
        result: BindingResult
    ): ModelAndView {
        val model = PasswordResetRequestModel(submitted.email)

        return if (result.hasErrors()) {
            AuthViews.PasswordResetRequest.render(model, HttpStatus.BAD_REQUEST)
        } else {
            return when (auth.requestPasswordReset(email = submitted.email)) {
                is PasswordResetRequestResult.Success ->
                    ModelAndView("redirect:/auth/request-password-reset?completed=true")
                is PasswordResetRequestResult.CouldNotFindUserWithEmail -> AuthViews.PasswordResetRequest.render(
                    model.copy(emailNotFound = true),
                    httpStatus = HttpStatus.BAD_REQUEST
                )
                is PasswordResetRequestResult.FailedUnexpectedly ->
                    throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @GetMapping("/password-reset")
    fun resetPasswordPage(@RequestParam("code") code: UUID): ModelAndView {
        val model = ResetPasswordViewModel(code)
        return AuthViews.ResetPassword.render(model)
    }

    @PostMapping("/password-reset")
    fun resetPassword(
        request: HttpServletRequest,
        @Valid @ModelAttribute("submitted") submitted: PasswordResetAttempt,
        result: BindingResult
    ): ModelAndView {
        if (!submitted.passwordsMatch) {
            result.addError(FieldError(result.objectName, "passwordConfirm", "Passwords don't match!"))
        }

        return if (result.hasErrors()) {
            AuthViews.ResetPassword.render(
                ResetPasswordViewModel(submitted.parsedCode()),
                httpStatus = HttpStatus.BAD_REQUEST
            )
        } else {
            when (auth.resetPassword(submitted.parsedCode(), submitted.email ?: "", submitted.password ?: "")) {
                PasswordResetResult.Success -> {
                    request.login(submitted.email, submitted.password)
                    ModelAndView("redirect:/dashboard")
                }
                PasswordResetResult.InvalidCredentials -> {
                    AuthViews.ResetPassword.render(
                        ResetPasswordViewModel(code = submitted.parsedCode(), badCredentials = true),
                        HttpStatus.BAD_REQUEST
                    )
                }
                PasswordResetResult.FailedUnexpectedly -> {
                    throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
                }
            }

        }
    }
}

