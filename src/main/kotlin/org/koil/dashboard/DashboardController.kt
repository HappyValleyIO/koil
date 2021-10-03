package org.koil.dashboard

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/dashboard")
class DashboardController {

    @GetMapping
    fun index(
        @RequestParam(defaultValue = "false") accountVerified: Boolean,
        @RequestParam(defaultValue = "false") accountAlreadyVerified: Boolean,
        @RequestParam(defaultValue = "false") incorrectVerificationCode: Boolean,
    ): ModelAndView = DashboardViews.Index.render(
        IndexViewModel(
            accountVerified,
            accountAlreadyVerified,
            incorrectVerificationCode
        )
    )

    @GetMapping("contact-us")
    fun contactUs(): ModelAndView = DashboardViews.ContactUs.render(Unit)
}

