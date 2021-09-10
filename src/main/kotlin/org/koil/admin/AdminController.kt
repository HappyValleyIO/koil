package org.koil.admin

import org.koil.user.EnrichedUserDetails
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView


@Controller
@RequestMapping("/admin")
class AdminController(private val adminService: IAdminService) {

    @GetMapping
    fun adminHome(@AuthenticationPrincipal user: EnrichedUserDetails, pageable: Pageable): ModelAndView {
        val accounts = adminService.getAccounts(user.accountId, pageable)
        val model = AdminIndexViewModel(user.handle, accounts)

        return AdminViews.AdminHomeView.render(model)
    }
}
