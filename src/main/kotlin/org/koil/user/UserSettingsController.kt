package org.koil.user

import org.koil.auth.EnrichedUserDetails
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import javax.validation.Valid

@Controller
@RequestMapping("/dashboard/user-settings")
class UserSettingsController(private val userService: UserService) {

    @GetMapping
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

    @PostMapping
    fun updateUserSettings(
        @AuthenticationPrincipal user: EnrichedUserDetails,
        @Valid @ModelAttribute("submitted") submitted: UpdateUserSettingsRequest,
        bindingResult: BindingResult
    ): ModelAndView {
        return if (!bindingResult.hasErrors()) {
            when (userService.updateUser(user.accountId, submitted)) {
                is AccountUpdateResult.AccountUpdated -> ModelAndView("redirect:/dashboard/user-settings?updated=true")
                is AccountUpdateResult.EmailAlreadyInUse -> ModelAndView("redirect:/dashboard/user-settings?updated=true&emailInUse=true")
            }
        } else {
            val account = userService.findUserById(user.accountId)
                ?: throw ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpectedly could not find user in database."
                )

            return UserViews.UserSettings.render(
                UserSettingsViewModel.from(
                    account,
                    updated = false,
                    emailInUse = false
                ),
                httpStatus = HttpStatus.BAD_REQUEST
            )
        }
    }
}
