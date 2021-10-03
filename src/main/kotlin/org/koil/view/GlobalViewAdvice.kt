package org.koil.view

import org.koil.auth.EnrichedUserDetails
import org.koil.auth.UserRole
import org.koil.user.Account
import org.koil.user.AccountRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute
import javax.servlet.http.HttpServletRequest

data class GlobalViewModel(val account: Account?) {
    fun isVerified(): Boolean = account?.isVerified() ?: false

    fun isAdmin(): Boolean =
        account?.isAdmin() ?: false

    fun isImpersonatingUser(): Boolean {
        val auth = SecurityContextHolder.getContext().authentication
        return auth?.let {
            auth.authorities.map { it.authority }.contains(UserRole.ADMIN_IMPERSONATING_USER.name)
        } ?: false
    }
}

/**
 * This controller advice is used to bulk out the default elements of the model that every request would need. While it's
 * probably a little bit of latency on every request it does mean that we can de-couple most of our controllers from having
 * to know too much about the cruft in rendering a page (e.g. username in the corner).
 */
@ControllerAdvice
class GlobalViewAdvice(
    private val accountRepository: AccountRepository
) {
    @ModelAttribute("global")
    fun globalModel(@AuthenticationPrincipal user: EnrichedUserDetails?): GlobalViewModel {
        val account = user?.let { accountRepository.findByIdOrNull(it.accountId) }
        return GlobalViewModel(account)
    }

    @ModelAttribute("_csrf")
    fun appendCSRFToken(request: HttpServletRequest): CsrfToken {
        return request.getAttribute(CsrfToken::class.java.name) as CsrfToken
    }
}
