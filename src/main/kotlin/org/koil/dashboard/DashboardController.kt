package org.koil.dashboard

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/dashboard")
class DashboardController {

    @GetMapping
    fun index(): ModelAndView = DashboardViews.Index.render(Unit)

    @GetMapping("contact-us")
    fun contactUs(): ModelAndView = DashboardViews.ContactUs.render(Unit)
}

