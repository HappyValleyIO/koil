package org.koil.user.verification

import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.recover
import org.koil.auth.EnrichedUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import java.util.*


@Controller
@RequestMapping("/dashboard/account-verification")
class AccountVerificationController(private val accountVerificationService: AccountVerificationService) {
    @GetMapping
    fun verifyAccount(
        @AuthenticationPrincipal user: EnrichedUserDetails,
        @RequestParam code: UUID
    ): ModelAndView {
        return accountVerificationService.verifyAccount(user.accountId, code)
            .map {
                ModelAndView("redirect:/dashboard?accountVerified=true")
            }.recover {
                when (it) {
                    AccountVerificationViolations.AccountAlreadyVerified ->
                        ModelAndView("redirect:/dashboard?accountAlreadyVerified=true")
                    AccountVerificationViolations.IncorrectCode ->
                        ModelAndView("redirect:/dashboard?incorrectVerificationCode=true")
                }
            }
    }
}
