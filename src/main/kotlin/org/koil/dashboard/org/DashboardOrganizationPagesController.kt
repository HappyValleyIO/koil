package org.koil.dashboard.org

import org.koil.auth.EnrichedUserDetails
import org.koil.org.OrganizationService
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView


@Controller
@RequestMapping("/dashboard/org")
class DashboardOrganizationPagesController(private val organizationService: OrganizationService) {

    @GetMapping
    fun organizationManagementHomePage(@AuthenticationPrincipal user: EnrichedUserDetails, pageable: Pageable): ModelAndView {
        val accounts = organizationService.getAccounts(user.accountId, pageable)
        val organization = organizationService.getOrganizationDetails(user.accountId)

        val model = OrgIndexViewModel(
            organizationName = organization.organizationName,
            organizationSignupLink = organization.signupLink,
            userName = user.handle,
            accounts = accounts
        )

        return OrgViews.OrgHomeView.render(model)
    }
}
