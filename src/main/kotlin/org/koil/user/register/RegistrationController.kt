package org.koil.user.register

import org.hibernate.validator.constraints.Length
import org.koil.auth.EnrichedUserDetails
import org.koil.user.UserCreationRequest
import org.koil.user.UserCreationResult
import org.koil.user.UserService
import org.koil.user.password.HashedPassword
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
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
) {
    fun toCreationRequest(): UserCreationRequest = UserCreationRequest(
        fullName = name,
        email = email,
        password = HashedPassword.encode(password),
        handle = handle
    )
}

@Controller
@RequestMapping("/auth")
class RegistrationController(
    @Autowired private val users: UserService,
) {
    @GetMapping("/register")
    fun register(
        @AuthenticationPrincipal user: EnrichedUserDetails?,
        @RequestParam("email", defaultValue = "") email: String
    ): ModelAndView {
        return if (user == null) {
            RegisterViews.Register.render(RegistrationViewModel(email))
        } else {
            ModelAndView("redirect:/dashboard")
        }
    }

    @PostMapping("/register")
    fun registerSubmit(
        request: HttpServletRequest,
        @Valid @ModelAttribute("submitted") submitted: RegistrationAttempt,
        result: BindingResult
    ): ModelAndView {
        return if (!result.hasErrors()) {
            when (users.createUser(submitted.toCreationRequest())) {
                is UserCreationResult.CreatedUser -> {
                    request.login(submitted.email, submitted.password)
                    ModelAndView("redirect:/dashboard")
                }
                is UserCreationResult.UserAlreadyExists -> {
                    RegisterViews.Register.render(
                        RegistrationViewModel(email = submitted.email, emailAlreadyTaken = true),
                        HttpStatus.BAD_REQUEST
                    )
                }
            }
        } else {
            RegisterViews.Register.render(
                RegistrationViewModel(email = submitted.email),
                HttpStatus.BAD_REQUEST
            )
        }
    }
}
