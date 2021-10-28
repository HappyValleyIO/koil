package org.koil.admin

import org.koil.auth.UserAuthority
import org.koil.user.Account
import org.koil.view.PaginatedViewModel
import org.koil.view.ViewRenderer
import org.springframework.data.domain.Page

data class AdminIndexViewModel(val userName: String, val accounts: Page<Account>) :
    PaginatedViewModel<Account>(accounts)

data class AdminAccountDetailsViewModel(
    val account: Account,
    val updated: Boolean = false,
    val emailAlreadyTaken: Boolean = false,
    val canBeAdmin: Boolean = false
) {
    val possibleAuthorities: List<Pair<UserAuthority, Boolean>> = UserAuthority.values().filter {
        canBeAdmin || it != UserAuthority.ADMIN
    }.map { authority ->
        authority to account.authorities.map { it.authority }.contains(authority)
    }
}

sealed class AdminViews<T>(override val template: String) : ViewRenderer<T> {
    object AdminHomeView : AdminViews<AdminIndexViewModel>("pages/admin/index")
    object AdminAccountDetailsView : AdminViews<AdminAccountDetailsViewModel>("pages/admin/accounts/account-details")
}
