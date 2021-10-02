package org.koil.view

import org.koil.auth.EnrichedUserDetails
import org.koil.auth.UserRole
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute
import javax.servlet.http.HttpServletRequest

data class GlobalViewModel(val user: EnrichedUserDetails?) {
    fun isAdmin(): Boolean =
        user?.isAdmin() ?: false

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
class GlobalViewAdvice {
    @ModelAttribute("global")
    fun globalModel(@AuthenticationPrincipal user: EnrichedUserDetails?): GlobalViewModel {
        return GlobalViewModel(user)
    }

    @ModelAttribute("_csrf")
    fun appendCSRFToken(request: HttpServletRequest): CsrfToken {
        return request.getAttribute(CsrfToken::class.java.name) as CsrfToken
    }
}
