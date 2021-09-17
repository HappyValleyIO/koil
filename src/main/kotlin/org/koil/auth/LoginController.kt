package org.koil.auth

import org.koil.user.EnrichedUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/auth")
class LoginController {
    @GetMapping("/login")
    fun viewLogin(
        @AuthenticationPrincipal principal: EnrichedUserDetails?,
        @RequestParam("redirect", required = false) redirect: String?,
        @RequestParam("error", required = false) error: String?,
    ): ModelAndView {
        return if (principal == null) {
            val model = LoginViewModel(redirect = redirect != null, badCredentials = error != null)
            AuthViews.Login.render(model)
        } else {
            return ModelAndView("redirect:/dashboard")
        }
    }
}
