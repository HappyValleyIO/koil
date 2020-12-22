package org.koil.user

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/dashboard")
class UserController(val views: IUserViews) {

    @GetMapping("user-settings")
    fun userSettings(@AuthenticationPrincipal user: EnrichedUserDetails): ModelAndView {
        val model = UserSettingsViewModel(
                handle = user.handle,
                bio = "Some example bio",
                email = user.details.username,
                notificationSettings = NotificationSettingsViewModel(weeklyActivity = true, updateOnMessage = false, reminderEmail = true)
        )

        return views.userSettingsView(model)
    }

    @GetMapping("contact-us")
    fun contactUs(): ModelAndView {
        return ModelAndView("pages/dashboard/contact-us", mapOf("" to ""))
    }
}
