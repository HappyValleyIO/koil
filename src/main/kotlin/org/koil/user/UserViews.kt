package org.koil.user

import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

data class NotificationSettingsViewModel(val weeklyActivity: Boolean, val updateOnMessage: Boolean, val reminderEmail: Boolean)
data class UserSettingsViewModel(val handle: String, val bio: String, val email: String, val notificationSettings: NotificationSettingsViewModel)

interface IUserViews {
    fun userSettingsView(userSettingsViewModel: UserSettingsViewModel): ModelAndView
}

@Component
class UserViews : IUserViews {
    override fun userSettingsView(userSettingsViewModel: UserSettingsViewModel): ModelAndView {
        return ModelAndView("pages/dashboard/user-settings", mapOf("model" to userSettingsViewModel))
    }
}
