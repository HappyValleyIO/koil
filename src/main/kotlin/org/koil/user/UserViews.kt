package org.koil.user

import org.koil.view.ViewRenderer

data class NotificationSettingsViewModel(
    val weeklyActivity: Boolean,
    val emailOnAccountChange: Boolean
)

data class UserSettingsViewModel(
    val name: String,
    val email: String,
    val notificationSettings: NotificationSettingsViewModel,
    val updated: Boolean,
    val emailInUse: Boolean
) {
    companion object {
        fun from(account: Account, updated: Boolean, emailInUse: Boolean): UserSettingsViewModel =
            UserSettingsViewModel(
                account.fullName, account.emailAddress,
                NotificationSettingsViewModel(
                    account.notificationSettings.weeklyActivity,
                    account.notificationSettings.emailOnAccountChange
                ),
                updated = updated,
                emailInUse = emailInUse
            )
    }

    val updateSuccess = !emailInUse && updated
    val updateFailed = emailInUse && updated
}

sealed class UserViews<T>(override val template: String) : ViewRenderer<T> {
    object UserSettings : UserViews<UserSettingsViewModel>("pages/dashboard/user-settings")
}
