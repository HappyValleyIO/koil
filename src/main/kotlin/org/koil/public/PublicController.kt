package org.koil.public

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

@Controller
class PublicController {
    @GetMapping("/")
    fun index(): ModelAndView {
        return ModelAndView("pages/index")
    }
}
