package org.koil.auth

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
}
