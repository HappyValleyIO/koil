package org.springboard.user

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.mapper.reflect.ColumnName
import org.springboard.auth.AuthAuthority
import org.springboard.schema.SchemaAccount
import org.springboard.schema.SchemaAccountCredentials
import org.springboard.schema.SchemaAuthAuthority
import org.springboard.schema.factory
import org.springframework.stereotype.Component
import java.time.Instant

interface UserPersistence {
    fun getUserByEmail(email: String): UserQueryResult?
    fun getUserByAccount(accountId: Long): UserQueryResult?
    fun createUser(request: UserCreationRequest): UserQueryResult
}

data class UserQueryResult(
        val accountId: Long,
        @ColumnName("email_address") val email: String,
        val password: String,
        val startDate: Instant,
        val fullName: String,
        val handle: String,
        val stopDate: Instant?,
        val authorities: List<AuthAuthority>
)

@Component
class UserPersistenceImpl(private val jdbi: Jdbi) : UserPersistence {

    override fun getUserByEmail(email: String): UserQueryResult? {
        return getUser(email = email)
    }

    override fun getUserByAccount(accountId: Long): UserQueryResult? {
        return getUser(accountId = accountId)
    }

    override fun createUser(request: UserCreationRequest): UserQueryResult {
        return jdbi.withHandle<UserQueryResult, RuntimeException> {handle ->
            val accountId = handle.createQuery(
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
                ),
                authorities AS (
                    INSERT INTO account_authorities
                        (account_id, authority_id)
                    SELECT 
                        account.account_id, auth_authorities.authority_id
                        FROM account
                        JOIN auth_authorities ON true = true
                        WHERE auth_authorities.authority_ref IN (<authorities>)
                )
                SELECT 
                    a.account_id
                FROM account a
            """
            )
                    .bind("email", request.email)
                    .bind("full_name", request.fullName)
                    .bind("password", request.password)
                    .bind("handle", request.handle)
                    .bindList("authorities", request.authorities.map { auth -> auth.ref })
                    .mapTo<Long>()
                    .one()

            getUserByAccount(accountId = accountId)
        }
    }

    private fun getUser(email: String? = null, accountId: Long? = null): UserQueryResult? {
        return jdbi.withHandle<UserQueryResult?, RuntimeException> {
            it.createQuery(
                    """
                    SELECT 
                        a.account_id as a_account_id, a.start_date as a_start_date, a.full_name as a_full_name, a.handle as a_handle, 
                            a.public_account_id as a_public_account_id, a.stop_date as a_stop_date,
                        c.account_id as c_account_id, c.email_address as c_email_address, c.password as c_password,
                        auth.authority_id as auth_authority_id, auth.authority_ref as auth_authority_ref, 
                            auth.authority_created as auth_authority_created
                    FROM accounts a
                        JOIN account_credentials c ON a.account_id = c.account_id
                        LEFT JOIN account_authorities ON a.account_id = account_authorities.account_id 
                        LEFT JOIN auth_authorities auth ON account_authorities.authority_id = auth.authority_id
                    WHERE (:email IS NULL OR email_address = :email)
                        AND (:account_id IS NULL OR a.account_id = :account_id)
    
            """
            ).bind("email", email)
                    .bind("account_id", accountId)
                    .registerRowMapper(factory(SchemaAccount::class.java, "a"))
                    .registerRowMapper(factory(SchemaAccountCredentials::class.java, "c"))
                    .registerRowMapper(factory(SchemaAuthAuthority::class.java, "auth"))
                    .reduceRows(null as UserQueryResult?) { acc, row ->
                        if (acc != null) {
                            val authority = row.getRow(SchemaAuthAuthority::class.java)
                            acc.copy(authorities = acc.authorities + authority.toDomain())
                        } else {
                            val account = row.getRow(SchemaAccount::class.java)
                            val accountCredentials = row.getRow(SchemaAccountCredentials::class.java)
                            val authority = row.getRow(SchemaAuthAuthority::class.java)

                            UserQueryResult(
                                    account.accountId, accountCredentials.emailAddress, accountCredentials.password, account.startDate, account.fullName, account.handle,
                                    account.stopDate, listOf(authority.toDomain())
                            )
                        }
                    }
        }
    }
}
