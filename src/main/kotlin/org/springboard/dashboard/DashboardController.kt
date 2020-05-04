package org.springboard.dashboard

import org.springboard.user.EnrichedUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/dashboard")
class DashboardController(
        val views: IDashboardViews
) {

    @GetMapping("")
    fun index(@AuthenticationPrincipal user: EnrichedUserDetails): ModelAndView {
        return views.renderIndex(DashboardIndexViewModel(user))
    }
}

