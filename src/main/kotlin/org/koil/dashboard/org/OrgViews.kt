package org.koil.dashboard.org

import org.koil.auth.UserAuthority
import org.koil.user.Account
import org.koil.view.PaginatedViewModel
import org.koil.view.ViewRenderer
import org.springframework.data.domain.Page
import java.util.*

data class OrgIndexViewModel(val organizationName: String, val organizationSignupLink: UUID, val userName: String, val accounts: Page<Account>) :
    PaginatedViewModel<Account>(accounts)

data class OrgAccountDetailsViewModel(
    val account: Account,
    val updated: Boolean = false,
    val emailAlreadyTaken: Boolean = false
) {
    val possibleAuthorities: List<Pair<UserAuthority, Boolean>> = UserAuthority.values().filter { it != UserAuthority.ADMIN }.map { authority ->
        authority to account.authorities.map { it.authority }.contains(authority)
    }
}

sealed class OrgViews<T>(override val template: String) : ViewRenderer<T> {
    object OrgHomeView : OrgViews<OrgIndexViewModel>("pages/dashboard/org/index")
    object OrgAccountDetailsOverview : OrgViews<OrgAccountDetailsViewModel>("pages/dashboard/org/accounts/account-details")
}
