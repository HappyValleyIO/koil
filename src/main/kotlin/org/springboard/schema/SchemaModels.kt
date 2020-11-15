package org.springboard.schema

import org.jdbi.v3.core.mapper.reflect.ColumnName
import org.springboard.auth.AuthAuthority
import java.time.Instant
import java.util.*

data class SchemaAccountAuthority(@ColumnName("account_id") val accountId: Long,
                                  @ColumnName("authority_id") val authorityId: Int,
                                  @ColumnName("authority_granted") val authorityGranted: Instant)

data class SchemaAccountCredentials(@ColumnName("account_id") val accountId: Long, @ColumnName("email_address") val emailAddress: String, @ColumnName("password") val password: String)

data class SchemaAccountPasswordReset(@ColumnName("account_id") val accountId: Long,
                                      @ColumnName("reset_code") val resetCode: UUID,
                                      @ColumnName("created_at") val createdAt: Instant,
                                      @ColumnName("expires_at") val expiresAt: Instant)

data class SchemaAccountVerification(@ColumnName("account_id") val accountId: Long,
                                     @ColumnName("verification_code") val verificationCode: UUID,
                                     @ColumnName("created_at") val createdAt: Instant,
                                     @ColumnName("expires_at") val expiresAt: Instant)

data class SchemaAccount(@ColumnName("account_id") val accountId: Long,
                         @ColumnName("start_date") val startDate: Instant,
                         @ColumnName("full_name") val fullName: String,
                         @ColumnName("stop_date") val stopDate: Instant?,
                         @ColumnName("handle") val handle: String,
                         @ColumnName("public_account_id") val publicAccountId: UUID)

data class SchemaAuthAuthority(@ColumnName("authority_id") val authorityId: Int,
                               @ColumnName("authority_ref") val authorityRef: String,
                               @ColumnName("authority_created") val authorityCreated: Instant) {
    fun toDomain(): AuthAuthority {
        return AuthAuthority.fromRef(authorityRef)
    }
}
