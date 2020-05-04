package org.springboard.user

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.mapper.reflect.ColumnName
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

interface UserPersistence {
    fun getUserByEmail(email: String): UserQueryResult?
    fun createUser(request: UserCreationRequest): UserQueryResult
}

data class UserQueryResult(
        val accountId: Long,
        @ColumnName("email_address") val email: String,
        val password: String,
        val startDate: OffsetDateTime,
        val fullName: String,
        val handle: String,
        val stopDate: OffsetDateTime?
)

@Component
class UserPersistenceImpl(private val jdbi: Jdbi) : UserPersistence {

    override fun getUserByEmail(email: String): UserQueryResult? {
        return jdbi.withHandle<UserQueryResult?, RuntimeException> {
            it.createQuery(
                    """
                SELECT a.*, uc.email_address, uc.password FROM account_credentials uc
                    JOIN accounts a ON uc.account_id = a.account_id
                    WHERE email_address = :email
            """
            ).bind("email", email)
                    .mapTo<UserQueryResult>()
                    .findFirst()
                    .orElse(null)
        }
    }

    override fun createUser(request: UserCreationRequest): UserQueryResult {
        return jdbi.withHandle<UserQueryResult, RuntimeException> {
            it.createQuery(
                    """
                WITH account AS (
                    INSERT INTO accounts
                        (full_name, handle)
                    VALUES
                        (:full_name, :handle)
                    RETURNING *
                ),
                credentials AS (
                INSERT INTO account_credentials
                    (account_id, email_address, password)
                SELECT
                    account.account_id, :email, :password
                    FROM account
                RETURNING *
                )
                SELECT a.*, c.email_address, c.password FROM account a
                    JOIN credentials c ON a.account_id = c.account_id
            """.trimIndent()
            )
                    .bind("email", request.email)
                    .bind("full_name", request.fullName)
                    .bind("password", request.password)
                    .bind("handle", request.handle)
                    .mapTo<UserQueryResult>()
                    .first()
        }
    }
}
