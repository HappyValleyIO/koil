package org.koil.admin

import org.koil.user.Account
import org.koil.view.PaginatedViewModel
import org.springframework.data.domain.Page
import org.springframework.web.servlet.ModelAndView

data class AdminIndexViewModel(val userName: String, val accounts: Page<Account>) :
    PaginatedViewModel<Account>(accounts)

data class AdminAccountDetailsViewModel(val userName: String, val account: Account)

sealed class AdminViews<T>(private val template: String) {

    fun render(model: T): ModelAndView {
        return ModelAndView(template, mapOf("model" to model))
    }

    object AdminHomeView : AdminViews<AdminIndexViewModel>("pages/admin/index")
    object AdminAccountDetailsView : AdminViews<AdminAccountDetailsViewModel>("pages/admin/accounts/account-details")
}
