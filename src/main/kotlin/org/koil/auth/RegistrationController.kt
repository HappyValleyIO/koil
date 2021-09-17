package org.koil.auth

import org.hibernate.validator.constraints.Length
import org.koil.user.UserCreationRequest
import org.koil.user.UserCreationResult
import org.koil.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern


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

@Controller
@RequestMapping("/auth")
class RegistrationController(
    @Autowired val users: UserService,
    @Autowired val auth: AuthService,
) {

    @GetMapping("/register")
    fun registrationPage(
        @RequestParam("email", defaultValue = "") email: String,
        model: MutableMap<String, Any>
    ): ModelAndView {
        return AuthViews.Register.render(RegistrationViewModel(RegistrationAttempt(email, "", "", "")))
    }

    @PostMapping("/register")
    fun registrationSubmit(
        request: HttpServletRequest,
        @Valid attempt: RegistrationAttempt,
        result: BindingResult
    ): ModelAndView {
        return if (!result.hasErrors()) {
            when (users.createUser(
                UserCreationRequest(
                    attempt.name,
                    attempt.email,
                    attempt.password,
                    attempt.handle
                )
            )) {
                is UserCreationResult.CreatedUser -> {
                    request.login(attempt.email, attempt.password)
                    ModelAndView("redirect:/dashboard")
                }
                is UserCreationResult.UserAlreadyExists -> {
                    AuthViews.Register.render(
                        RegistrationViewModel(
                            errors = mutableMapOf("email" to "A user with that email address already exists"),
                            attempt = attempt
                        ),
                        HttpStatus.BAD_REQUEST
                    )
                }
            }
        } else {
            AuthViews.Register.render(
                RegistrationViewModel(errors = result.errors().toMutableMap(), attempt = attempt),
                HttpStatus.BAD_REQUEST
            )
        }
    }
}
