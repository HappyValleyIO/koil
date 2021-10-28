package org.koil.user.register

import org.koil.view.ViewRenderer
import java.util.*

data class RegistrationViewModel(
    val email: String,
    val emailAlreadyTaken: Boolean = false,
    val signupLink: UUID
)

data class CompanyRegistrationViewModel(
    val email: String,
    val emailAlreadyTaken: Boolean = false
)

sealed class RegisterViews<T>(override val template: String) : ViewRenderer<T> {
    object RegisterEmployee : RegisterViews<RegistrationViewModel>("pages/register-employee")
    object RegisterCompany : RegisterViews<CompanyRegistrationViewModel>("pages/register-company")
}

