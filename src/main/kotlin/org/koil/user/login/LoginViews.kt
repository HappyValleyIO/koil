package org.koil.user.login

import org.koil.view.ViewRenderer

data class LoginViewModel(
    val redirect: Boolean = false,
    val badCredentials: Boolean = false
)

sealed class LoginViews<T>(override val template: String) : ViewRenderer<T> {
    object Login : LoginViews<LoginViewModel>("pages/login")
}
