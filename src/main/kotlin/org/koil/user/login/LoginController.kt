package org.koil.user.login

import org.koil.auth.EnrichedUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/auth/login")
class LoginController {
    @GetMapping
    fun viewLogin(
        @AuthenticationPrincipal principal: EnrichedUserDetails?,
        @RequestParam("redirect", required = false) redirect: String?,
        @RequestParam("error", required = false) error: String?,
    ): ModelAndView {
        return if (principal == null) {
            val model = LoginViewModel(redirect = redirect != null, badCredentials = error != null)
            LoginViews.Login.render(model)
        } else {
            return ModelAndView("redirect:/dashboard")
        }
    }
}
