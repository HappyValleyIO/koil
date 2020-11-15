package org.springboard.user

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.mapper.reflect.ColumnName
import org.springboard.auth.AuthAuthority
import org.springboard.schema.SchemaAccount
import org.springboard.schema.SchemaAuthAuthority
import org.springboard.schema.factory
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

interface UserPersistence {
    fun getUserByEmail(email: String): UserQueryResult?
    fun getUserByAccount(accountId: Long): InternalUser?
    fun createUser(request: UserToStore): InternalUser
}

interface InternalUserPersistence {
    fun getUsers(email: String? = null, queryByAccountId: Long? = null): List<UserQueryResult>
}

data class UserToStore(
        val fullName: String,
        val email: String,
        val password: String,
        val handle: String,
        val authorities: List<AuthAuthority> = listOf(AuthAuthority.USER)
)

data class UserQueryResult(
        override val accountId: Long,
        @ColumnName("email_address") override val email: String,
        val password: String,
        val startDate: Instant,
        override val fullName: String,
        override val handle: String,
        val stopDate: Instant?,
        override val authorities: List<AuthAuthority>,
        override val publicId: UUID
) : InternalUser

@Component
class UserPersistenceImpl(private val jdbi: Jdbi) : UserPersistence, InternalUserPersistence {

    override fun getUserByEmail(email: String): UserQueryResult? {
        return getUser(email = email)
    }

    override fun getUserByAccount(accountId: Long): UserQueryResult? {
        return getUser(queryByAccountId = accountId)
    }

    override fun createUser(request: UserToStore): UserQueryResult {
        return jdbi.withHandle<UserQueryResult, RuntimeException> { handle ->
            val accountId = handle.createQuery(
                    """
                WITH account AS (
                    INSERT INTO accounts
                        (full_name, handle, email_address, password)
                    VALUES
                        (:full_name, :handle, :email, :password)
                    RETURNING *
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

    private fun getUser(email: String? = null, queryByAccountId: Long? = null): UserQueryResult? {
        require(email != null || queryByAccountId != null) {
            "We shouldn't be attempting to query for a single user without specifying some search criteria"
        }

        return getUsers(email, queryByAccountId).firstOrNull()
    }

    override fun getUsers(email: String?, queryByAccountId: Long?): List<UserQueryResult> {
        return jdbi.withHandle<List<UserQueryResult>, RuntimeException> { handle ->
            handle.createQuery(
                    """
                    SELECT 
                        a.account_id as a_account_id, a.start_date as a_start_date, a.full_name as a_full_name, a.handle as a_handle, 
                            a.public_account_id as a_public_account_id, a.stop_date as a_stop_date,
                            a.email_address as a_email_address, a.password as a_password,
                        auth.authority_id as auth_authority_id, auth.authority_ref as auth_authority_ref, 
                            auth.authority_created as auth_authority_created
                    FROM accounts a
                        LEFT JOIN account_authorities ON a.account_id = account_authorities.account_id 
                        LEFT JOIN auth_authorities auth ON account_authorities.authority_id = auth.authority_id
                    WHERE (:email IS NULL OR email_address = :email)
                        AND (:account_id IS NULL OR a.account_id = :account_id)
    
            """
            ).bind("email", email)
                    .bind("account_id", queryByAccountId)
                    .registerRowMapper(factory(SchemaAccount::class.java, "a"))
                    .registerRowMapper(factory(SchemaAuthAuthority::class.java, "auth"))
                    .reduceRows(mutableMapOf<Long, UserQueryResult>()) { acc, row ->
                        val account = row.getRow(SchemaAccount::class.java)
                        val existing = acc[account.accountId]

                        if (existing != null) {
                            val authority = row.getRow(SchemaAuthAuthority::class.java)
                            val updated = existing.copy(authorities = existing.authorities + authority.toDomain())
                            acc[account.accountId] = updated
                        } else {
                            val authority = row.getRow(SchemaAuthAuthority::class.java)
                            with(account) {
                                val toAdd = UserQueryResult(
                                        accountId, emailAddress, password, startDate, fullName, account.handle,
                                        stopDate, listOf(authority.toDomain()), publicAccountId
                                )
                                acc[account.accountId] = toAdd
                            }
                        }
                        acc
                    }
                    .values
                    .toList()
        }
    }
}
