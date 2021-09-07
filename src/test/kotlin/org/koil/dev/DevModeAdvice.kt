package org.koil.dev

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
@Profile("dev")
class DevModeAdvice {
    @ModelAttribute("dev")
    fun enableDevMode(): Boolean {
        return true
    }
}
