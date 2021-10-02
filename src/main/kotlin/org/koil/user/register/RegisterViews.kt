package org.koil.user.register

import org.koil.view.ViewRenderer

data class RegistrationViewModel(
    val email: String,
    val emailAlreadyTaken: Boolean = false
)

sealed class RegisterViews<T>(override val template: String) : ViewRenderer<T> {
    object Register : RegisterViews<RegistrationViewModel>("pages/register")
}

