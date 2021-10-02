package org.koil.user.password

sealed class PasswordResetRequestResult {
    object Success : PasswordResetRequestResult()
    object FailedUnexpectedly : PasswordResetRequestResult()
    object CouldNotFindUserWithEmail : PasswordResetRequestResult()
}

sealed class PasswordResetResult {
    object Success : PasswordResetResult()
    object InvalidCredentials : PasswordResetResult()
    object FailedUnexpectedly : PasswordResetResult()
}
