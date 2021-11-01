package org.koil.dashboard.org.accounts

import org.hibernate.validator.constraints.Length
import org.koil.auth.EnrichedUserDetails
import org.koil.auth.UserAuthority
import org.koil.dashboard.org.OrgAccountDetailsViewModel
import org.koil.dashboard.org.OrgViews
import org.koil.org.OrgAccountUpdateResult
import org.koil.org.OrganizationService
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
    val normalizedEmail: String = email.trim().lowercase()

    fun update(account: Account): Account =
        account
            .withAuthorities(authorities)
            .updateEmail(normalizedEmail)
            .updateName(fullName)
            .updateHandle(handle)

    fun containsOnlyAllowedAuthorities(): Boolean {
        return !authorities.contains(UserAuthority.ADMIN)
    }
}

@Controller
@RequestMapping("/dashboard/org")
class OrgAccountsController(private val orgService: OrganizationService) {
    @GetMapping("/accounts/{accountId}")
    fun adminAccountDetails(
        @AuthenticationPrincipal user: EnrichedUserDetails,
        @PathVariable("accountId") accountId: Long,
        @RequestParam("updated", defaultValue = "false") updated: Boolean
    ): ModelAndView {
        val account = orgService.getAccount(user.accountId, accountId)

        return if (account != null) {
            OrgViews.OrgAccountDetailsOverview.render(OrgAccountDetailsViewModel(account, updated))
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
            val account = orgService.getAccount(user.accountId, accountId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")

            return OrgViews.OrgAccountDetailsOverview.render(OrgAccountDetailsViewModel(account, false))
                .apply { status = HttpStatus.BAD_REQUEST }
        }

        return when (val result = orgService.updateAccount(user.accountId, accountId, submitted)) {
            is OrgAccountUpdateResult.AccountUpdateSuccess -> {
                ModelAndView("redirect:/dashboard/org/accounts/$accountId?updated=true")
            }
            OrgAccountUpdateResult.CouldNotFindAccount -> {
                throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Unable to find resource"
                )
            }
            is OrgAccountUpdateResult.EmailAlreadyTaken -> OrgViews.OrgAccountDetailsOverview.render(
                OrgAccountDetailsViewModel(result.account, updated = false, emailAlreadyTaken = true)
            )
                .apply { status = HttpStatus.BAD_REQUEST }
        }
    }
}
