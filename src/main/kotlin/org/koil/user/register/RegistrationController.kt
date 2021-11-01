package org.koil.user.register

import org.hibernate.validator.constraints.Length
import org.koil.auth.EnrichedUserDetails
import org.koil.org.OrganizationCreatedResult
import org.koil.org.OrganizationService
import org.koil.org.OrganizationSetupRequest
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
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class IndividualRegistrationAttempt(
    @get:Email(message = "Must be a valid email address") val email: String,
    @get:Length(min = 4, max = 16, message = "Handle must be between 4 and 16 chars long")
    @get:Pattern(
        regexp = "^[a-zA-Z0-9_]*$",
        message = "Handle can only contain alphanumeric characters (letters A-Z and number 0-9) or underscores."
    ) val handle: String,
    @get:Length(min = 8, message = "Password must be at least 8 characters long") val password: String,
    @get:NotEmpty(message = "Name cannot be empty") val name: String,
    @get:NotNull(message = "SignupLink cannot be empty") val signupLink: UUID
) {
    fun toCreationRequest(): UserCreationRequest = UserCreationRequest(
        signupLink = signupLink,
        fullName = name,
        email = email,
        password = HashedPassword.encode(password),
        handle = handle
    )
}

data class OrganizationRegistrationAttempt(
    @get:Email(message = "Must be a valid email address") val email: String,
    @get:Length(min = 4, max = 16, message = "Handle must be between 4 and 16 chars long")
    @get:Pattern(
        regexp = "^[a-zA-Z0-9_]*$",
        message = "Handle can only contain alphanumeric characters (letters A-Z and number 0-9) or underscores."
    ) val handle: String,
    @get:Length(min = 8, message = "Password must be at least 8 characters long") val password: String,
    @get:NotEmpty(message = "Name cannot be empty") val name: String,
    @get:NotEmpty(message = "Organization Name cannot be empty")
    val organizationName: String
) {
    fun toSetupRequest(): OrganizationSetupRequest = OrganizationSetupRequest(
        organizationName = organizationName,
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
    @Autowired private val companies: OrganizationService
) {
    @GetMapping("/register/individual")
    fun registerIndividual(
        @AuthenticationPrincipal user: EnrichedUserDetails?,
        @RequestParam("email", defaultValue = "") email: String,
        @RequestParam("signupLink") signupLink: UUID?,
    ): ModelAndView {
        return if (user != null) {
            ModelAndView("redirect:/dashboard")
        } else if (signupLink == null) {
            ModelAndView("redirect:/auth/register/organization?email=$email")
        } else {
            RegisterViews.RegisterIndividual.render(RegistrationViewModel(email, signupLink = signupLink))
        }
    }

    @GetMapping("/register/organization")
    fun registerOrganization(
        @AuthenticationPrincipal user: EnrichedUserDetails?,
        @RequestParam("email", defaultValue = "") email: String,
    ): ModelAndView {
        return if (user != null) {
            ModelAndView("redirect:/dashboard")
        } else {
            RegisterViews.RegisterOrganization.render(OrganizationRegistrationViewModel(email))
        }
    }

    @PostMapping("/register/organization")
    fun registerOrganizationSubmit(
        request: HttpServletRequest,
        @Valid @ModelAttribute("submitted") submitted: OrganizationRegistrationAttempt,
        result: BindingResult
    ): ModelAndView {
        return if (!result.hasErrors()) {
            when (companies.setupOrganization(submitted.toSetupRequest())) {
                is OrganizationCreatedResult.CreatedOrganization -> {
                    request.login(submitted.email, submitted.password)
                    ModelAndView("redirect:/dashboard")
                }
                is OrganizationCreatedResult.UserCreationFailed -> {
                    RegisterViews.RegisterOrganization.render(
                        OrganizationRegistrationViewModel(email = submitted.email, emailAlreadyTaken = true),
                        HttpStatus.BAD_REQUEST
                    )
                }
                is OrganizationCreatedResult.CreationFailed -> {
                    RegisterViews.RegisterOrganization.render(
                        OrganizationRegistrationViewModel(email = submitted.email, emailAlreadyTaken = true),
                        HttpStatus.INTERNAL_SERVER_ERROR
                    )
                }
            }
        } else {
            RegisterViews.RegisterOrganization.render(
                OrganizationRegistrationViewModel(email = submitted.email),
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @PostMapping("/register/individual")
    fun registerIndividualSubmit(
        request: HttpServletRequest,
        @Valid @ModelAttribute("submitted") submitted: IndividualRegistrationAttempt,
        result: BindingResult
    ): ModelAndView {
        return if (!result.hasErrors()) {
            when (users.createUser(submitted.toCreationRequest())) {
                is UserCreationResult.CreatedUser -> {
                    request.login(submitted.email, submitted.password)
                    ModelAndView("redirect:/dashboard")
                }
                is UserCreationResult.UserAlreadyExists,
                is UserCreationResult.InvalidSignupLink -> {
                    RegisterViews.RegisterIndividual.render(
                        RegistrationViewModel(
                            email = submitted.email,
                            emailAlreadyTaken = true,
                            signupLink = submitted.signupLink
                        ),
                        HttpStatus.BAD_REQUEST
                    )
                }
            }
        } else {
            RegisterViews.RegisterIndividual.render(
                RegistrationViewModel(email = submitted.email, signupLink = submitted.signupLink),
                HttpStatus.BAD_REQUEST
            )
        }
    }
}
