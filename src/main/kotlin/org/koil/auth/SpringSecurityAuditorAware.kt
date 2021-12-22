package org.koil.auth

import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.*

@Component
class SpringSecurityAuditorAware : AuditorAware<Long> {
    override fun getCurrentAuditor(): Optional<Long> {
        val auth = SecurityContextHolder.getContext().authentication?.principal

        return if (auth is EnrichedUserDetails) {
            Optional.of(auth.accountId)
        } else {
            Optional.empty()
        }
    }
}
