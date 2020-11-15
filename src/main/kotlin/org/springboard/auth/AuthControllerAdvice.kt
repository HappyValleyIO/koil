package org.springboard.auth

import org.springboard.user.EnrichedUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class CsrfControllerAdvice {
    @ModelAttribute("_csrf")
    fun appendCSRFToken(request: HttpServletRequest): CsrfToken {
        return request.getAttribute(CsrfToken::class.java.name) as CsrfToken
    }

    @ModelAttribute("isAdmin")
    fun isAdmin(@AuthenticationPrincipal details: EnrichedUserDetails?): Boolean {
        return details?.isAdmin() ?: false
    }

    @ModelAttribute("isImpersonatingUser")
    fun isImpersonatingUser(): Boolean {
        val auth = SecurityContextHolder.getContext().authentication
        return auth?.let {
            auth.authorities.map { it.authority }.contains(AuthRole.ADMIN_IMPERSONATING_USER.ref)
        } ?: false
    }
}
