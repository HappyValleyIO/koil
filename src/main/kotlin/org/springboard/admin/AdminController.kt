package org.springboard.admin

import org.springboard.user.Account
import org.springboard.user.EnrichedUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

data class AdminIndexViewModel(val userName: String, val accounts: List<Account>)

sealed class AdminViews<T>(private val template: String) {

    fun render(model: T): ModelAndView {
        return ModelAndView(template, mapOf("model" to model))
    }

    object AdminHomeView : AdminViews<AdminIndexViewModel>("pages/admin/index")
}

@Controller
@RequestMapping("/admin")
class AdminController(private val adminService: IAdminService) {

    @GetMapping
    fun adminHome(@AuthenticationPrincipal user: EnrichedUserDetails): ModelAndView {
        val accounts = adminService.getAccounts(user.accountId)
        val model = AdminIndexViewModel(user.handle, accounts)

        return AdminViews.AdminHomeView.render(model)
    }
}