package org.koil.user

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface AccountRepository : PagingAndSortingRepository<Account, Long> {

    fun findAccountByEmailAddressIgnoreCase(email: String): Account?

    @Query(
        """
        SELECT a.*, apr.* FROM accounts a
        JOIN account_password_reset apr ON a.account_id = apr.account_id
        WHERE apr.reset_code = :resetCode
    """
    )
    fun findAccountByPasswordResetCode(resetCode: UUID): Account?
}

