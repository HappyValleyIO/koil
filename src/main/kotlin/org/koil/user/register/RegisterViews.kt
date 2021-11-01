package org.koil.user.register

import org.koil.view.ViewRenderer
import java.util.*

data class RegistrationViewModel(
    val email: String,
    val emailAlreadyTaken: Boolean = false,
    val signupLink: UUID
)

data class OrganizationRegistrationViewModel(
    val email: String,
    val emailAlreadyTaken: Boolean = false
)

sealed class RegisterViews<T>(override val template: String) : ViewRenderer<T> {
    object RegisterIndividual : RegisterViews<RegistrationViewModel>("pages/register-individual")
    object RegisterOrganization : RegisterViews<OrganizationRegistrationViewModel>("pages/register-organization")
}

