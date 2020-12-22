package org.koil.auth

import org.hibernate.validator.constraints.Length
import org.koil.user.EnrichedUserDetails
import org.koil.user.UserCreationRequest
import org.koil.user.UserCreationResult
import org.koil.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern


data class PasswordResetAttempt(
        @get:Pattern(
                regexp = "[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}",
                message = "Code given is not a valid reset code"
        ) val code: String,
        @get:Email(message = "Must be a valid email address") val email: String?,
        @get:Length(min = 8, message = "Password must be at least 8 characters long") val password: String?,
        val passwordConfirm: String?
)


data class PasswordResetRequest(@get:Email(message = "Must be a valid email address") val email: String)

data class RegistrationAttempt(
        @get:Email(message = "Must be a valid email address") val email: String,
        @get:Length(min = 4, max = 16, message = "Handle must be between 4 and 16 chars long")
        @get:Pattern(
                regexp = "^[a-zA-Z0-9_]*$",
                message = "Handle can only contain alphanumeric characters (letters A-Z and number 0-9) or underscores."
        ) val handle: String,
        @get:Length(min = 8, message = "Password must be at least 8 characters long") val password: String,
        @get:NotEmpty(message = "Name cannot be empty") val name: String
)

data class LoginAttempt(
        @get:Email(message = "Must be a valid email address") val email: String,
        @get:Length(min = 8, message = "Password must be at least 8 characters long") val password: String
)

@Controller
@RequestMapping("/auth")
class AuthController(@Autowired val users: UserService,
                     @Autowired val auth: AuthService,
                     @Autowired val views: IAuthViews) {

    @GetMapping("/login")
    fun viewLogin(
            @AuthenticationPrincipal principal: EnrichedUserDetails?,
            @RequestParam("redirect", required = false) redirect: String?
    ): ModelAndView {
        if (principal !== null) {
            return ModelAndView("redirect:/dashboard")
        }

        return views.login(LoginViewModel(redirect = redirect != null))
    }

    @PostMapping("/login")
    fun verifyLogin(@Valid attempt: LoginAttempt, result: BindingResult, request: HttpServletRequest): ModelAndView {
        if (result.hasErrors()) {
            return views.login(LoginViewModel(email = attempt.email, errors = result.errors()))
        }

        return try {
            request.login(attempt.email, attempt.password)
            ModelAndView("redirect:/dashboard")
        } catch (e: Throwable) {
            views.login(LoginViewModel(badCredentials = true))
        }
    }

    @GetMapping("/request-password-reset")
    fun requestPasswordReset(): ModelAndView {
        return views.requestPasswordReset(PasswordResetRequestModel())
    }

    @PostMapping("/request-password-reset")
    fun requestPasswordReset(
            @Valid form: PasswordResetRequest,
            result: BindingResult
    ): ModelAndView {
        val model = PasswordResetRequestModel(form, result.errors().toMutableMap(), false)

        return if (result.hasErrors()) {
            views.requestPasswordReset(model)
        } else {
            return when (auth.requestPasswordReset(email = form.email)) {
                is PasswordResetRequestResult.Success -> views.requestPasswordReset(model.copy(completed = true))
                is PasswordResetRequestResult.CouldNotFindUserWithEmail -> views.requestPasswordReset(model.copy(completed = true))
                is PasswordResetRequestResult.FailedUnexpectedly -> {
                    model.errors["unexpected"] = """
                      An unexpected error has occurred - please try again. If you continue to experience the issue then please get in touch at support@getkoil.dev
                      """.trimIndent()

                    views.requestPasswordReset(model.copy(completed = true))
                }
            }
        }
    }

    @GetMapping("/password-reset")
    fun resetPasswordPage(@RequestParam("code") code: String?): ModelAndView {
        val model = ResetPasswordViewModel(code = code)

        val uuid: UUID? = try {
            UUID.fromString(code)
        } catch (e: Throwable) {
            null
        }
        if (uuid == null) {
            model.errors["code"] =
                    "Whoops! It looks like you've arrived at this page without a valid reset code. Please click the button in your email again."
        }

        return views.resetPassword(model)
    }

    @PostMapping("/password-reset")
    fun resetPassword(
            request: HttpServletRequest,
            @Valid attempt: PasswordResetAttempt,
            result: BindingResult
    ): ModelAndView {
        val model = ResetPasswordViewModel(attempt, result.errors().toMutableMap(), attempt.code)

        val uuid: UUID? = try {
            UUID.fromString(attempt.code)
        } catch (e: Throwable) {
            null
        }

        if (uuid == null) {
            model.errors["code"] =
                    "Whoops! It looks like you've arrived at this page without a valid reset code. Please click the button in your email again."
        }

        if (!attempt.password.equals(attempt.passwordConfirm)) {
            model.errors["password-confirm"] = "Passwords must match!"
        }

        return if (model.errors.keys.size != 0) {
            return views.resetPassword(model)
        } else {
            // Attempt to reset the password
            auth.resetPassword(uuid!!, attempt.email ?: "", attempt.password ?: "")
            request.login(attempt.email, attempt.password)
            ModelAndView("redirect:/dashboard")
        }
    }

    @GetMapping("/register")
    fun registrationPage(
            @RequestParam("email", defaultValue = "") email: String,
            model: MutableMap<String, Any>
    ): ModelAndView {
        return views.register(RegistrationViewModel(RegistrationAttempt(email, "", "", "")))
    }

    @PostMapping("/register")
    fun registrationSubmit(
            request: HttpServletRequest,
            @Valid attempt: RegistrationAttempt,
            result: BindingResult
    ): ModelAndView {
        return if (!result.hasErrors()) {
            when (users.createUser(UserCreationRequest(attempt.name, attempt.email, attempt.password, attempt.handle))) {
                is UserCreationResult.CreatedUser -> {
                    request.login(attempt.email, attempt.password)
                    ModelAndView("redirect:/dashboard")
                }
                is UserCreationResult.UserAlreadyExists -> {
                    views.register(RegistrationViewModel(errors = mutableMapOf("email" to "A user with that email address already exists"), attempt = attempt))
                }
            }
        } else {
            views.register(RegistrationViewModel(errors = result.errors().toMutableMap(), attempt = attempt))
        }
    }
}

fun BindingResult.errors(): Map<String, String?> {
    return this.fieldErrors.map { Pair(it.field, it.defaultMessage) }.toMap()
}
