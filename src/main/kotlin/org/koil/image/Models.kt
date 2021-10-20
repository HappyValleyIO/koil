package org.koil.image

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("account_image")
data class AccountImage(
    @Id val id: Long?,
    val publicId: UUID,
    val accountId: Long,
    @LastModifiedDate val updatedAt: Instant
) {
    companion object {
        fun createForAccount(accountId: Long): AccountImage =
            AccountImage(
                id = null,
                publicId = UUID.randomUUID(),
                accountId = accountId,
                updatedAt = Instant.now()
            )
    }
}
