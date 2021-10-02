package org.koil.admin.accounts

import org.hibernate.validator.constraints.Length
import org.koil.admin.AdminAccountDetailsViewModel
import org.koil.admin.AdminAccountUpdateResult
import org.koil.admin.AdminService
import org.koil.admin.AdminViews
import org.koil.auth.EnrichedUserDetails
import org.koil.auth.UserAuthority
import org.koil.user.Account
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

data class UpdateAccountRequest(
    @get:NotEmpty(message = "Name cannot be empty") val fullName: String,
    @get:Email(message = "Must be a valid email address") val email: String,
    @get:Length(min = 4, max = 16, message = "Handle must be between 4 and 16 chars long") val handle: String,
    val authorities: List<UserAuthority>
) {
    val normalizedEmail: String = email.trim().toLowerCase()

    fun update(account: Account): Account =
        account
            .withAuthorities(authorities)
            .updateEmail(normalizedEmail)
            .updateName(fullName)
            .updateHandle(handle)
}

@Controller
@RequestMapping("/admin")
class AdminAccountsController(private val adminService: AdminService) {
    @GetMapping("/accounts/{accountId}")
    fun adminAccountDetails(
        @AuthenticationPrincipal user: EnrichedUserDetails,
        @PathVariable("accountId") accountId: Long,
        @RequestParam("updated", defaultValue = "false") updated: Boolean
    ): ModelAndView {
        val account = adminService.getAccount(user.accountId, accountId)

        return if (account != null) {
            AdminViews.AdminAccountDetailsView.render(AdminAccountDetailsViewModel(account, updated))
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")
        }
    }

    @PostMapping("/accounts/{accountId}")
    fun updateAccountDetails(
        @AuthenticationPrincipal user: EnrichedUserDetails,
        @PathVariable("accountId") accountId: Long,
        @Valid @ModelAttribute("submitted") submitted: UpdateAccountRequest,
        bindingResult: BindingResult
    ): ModelAndView {
        if (bindingResult.hasErrors()) {
            val account = adminService.getAccount(user.accountId, accountId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")

            return AdminViews.AdminAccountDetailsView.render(AdminAccountDetailsViewModel(account, false))
                .apply { status = HttpStatus.BAD_REQUEST }
        }

        return when (val result = adminService.updateAccount(user.accountId, accountId, submitted)) {
            is AdminAccountUpdateResult.AccountUpdateSuccess -> {
                ModelAndView("redirect:/admin/accounts/$accountId?updated=true")
            }
            AdminAccountUpdateResult.CouldNotFindAccount -> {
                throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Unable to find resource"
                )
            }
            is AdminAccountUpdateResult.EmailAlreadyTaken -> AdminViews.AdminAccountDetailsView.render(
                AdminAccountDetailsViewModel(result.account, updated = false, emailAlreadyTaken = true)
            )
                .apply { status = HttpStatus.BAD_REQUEST }
        }
    }
}
