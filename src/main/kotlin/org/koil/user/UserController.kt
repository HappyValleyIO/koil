package org.koil.user

import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import javax.validation.Valid

@Controller
@RequestMapping("/dashboard")
class UserController(private val userService: UserService) {

    @GetMapping("user-settings")
    fun userSettings(
        @AuthenticationPrincipal user: EnrichedUserDetails,
        @RequestParam(defaultValue = "false") updated: Boolean,
        @RequestParam(defaultValue = "false") emailInUse: Boolean
    ): ModelAndView {
        val account = userService.findUserById(user.accountId)
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpectedly could not find user in database."
            )


        return UserViews.UserSettings.render(
            UserSettingsViewModel.from(
                account,
                updated = updated,
                emailInUse = emailInUse
            )
        )
    }

    @PostMapping("user-settings")
    fun updateUserSettings(
        @AuthenticationPrincipal user: EnrichedUserDetails,
        @Valid request: UpdateUserSettingsRequest
    ): ModelAndView {
        return when (userService.updateUser(user.accountId, request)) {
            is AccountUpdateResult.AccountUpdated -> ModelAndView("redirect:/dashboard/user-settings?updated=true")
            is AccountUpdateResult.EmailAlreadyInUse -> ModelAndView("redirect:/dashboard/user-settings?updated=true&emailInUse=true")
        }
    }

    @GetMapping("contact-us")
    fun contactUs(): ModelAndView {
        return ModelAndView("pages/dashboard/contact-us", mapOf("" to ""))
    }
}
