package org.koil.org

import org.koil.user.Account
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("organization")
data class Organization(
    @Id val organizationId: Long?,
    val organizationName: String,
    val startDate: Instant,
    val stopDate: Instant?,
    val signupLink: UUID
)

sealed class OrgAccountUpdateResult {
    data class AccountUpdateSuccess(val account: Account) : OrgAccountUpdateResult()
    data class EmailAlreadyTaken(val account: Account) : OrgAccountUpdateResult()
    object CouldNotFindAccount : OrgAccountUpdateResult()
}
