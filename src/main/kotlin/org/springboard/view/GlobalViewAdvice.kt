package org.springboard.view

import org.springboard.user.EnrichedUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

data class GlobalViewModel(val user: EnrichedUserDetails?)

/**
 * This controller advice is used to bulk out the default elements of the model that every request would need. While it's
 * probably a little bit of latency on every request it does mean that we can de-couple most of our controllers from having
 * to know too much about the cruft in rendering a page (e.g. username in the corner).
 */
@ControllerAdvice
class GlobalViewAdvice {
    @ModelAttribute("global")
    fun globalModel(@AuthenticationPrincipal user: EnrichedUserDetails?): GlobalViewModel {
        return GlobalViewModel(user)
    }
}
