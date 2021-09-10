package org.koil.admin.accounts

import org.koil.admin.AdminAccountDetailsViewModel
import org.koil.admin.AdminViews
import org.koil.admin.IAdminService
import org.koil.user.EnrichedUserDetails
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView


@Controller
@RequestMapping("/admin")
class AdminAccountsController(private val adminService: IAdminService) {
    @GetMapping("/accounts/{accountId}")
    fun adminAccountDetails(
        @AuthenticationPrincipal user: EnrichedUserDetails,
        @PathVariable("accountId") accountId: Long
    ): ModelAndView {
        val account = adminService.getAccount(user.accountId, accountId)

        return if (account != null) {
            AdminViews.AdminAccountDetailsView.render(AdminAccountDetailsViewModel(user.handle, account))
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")
        }
    }
}
