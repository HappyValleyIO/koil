package org.koil.dashboard.org

import org.koil.auth.EnrichedUserDetails
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView


@Controller
@RequestMapping("/dashboard/org")
class OrgController(private val orgService: OrgService) {

    @GetMapping
    fun adminHome(@AuthenticationPrincipal user: EnrichedUserDetails, pageable: Pageable): ModelAndView {
        val accounts = orgService.getAccounts(user.accountId, pageable)
        val company = orgService.getCompanyDetails(user.accountId)

        val model = OrgIndexViewModel(
            companyName = company.companyName,
            companySignupLink = company.signupLink,
            userName = user.handle,
            accounts = accounts
        )

        return OrgViews.OrgHomeView.render(model)
    }
}
