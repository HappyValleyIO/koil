package org.koil.user

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
interface AccountRepository : PagingAndSortingRepository<Account, Long> {

    fun existsAccountByEmailAddressIgnoreCase(email: String): Boolean

    fun findAccountByEmailAddressIgnoreCase(email: String): Account?

    fun findAccountsByOrganizationId(organizationId: Long, pageable: Pageable): Page<Account>

    @Query(
        """
        SELECT a.*, apr.* FROM accounts a
        LEFT JOIN account_password_reset apr ON a.account_id = apr.account_id
        WHERE apr.reset_code = :resetCode
    """
    )
    fun findAccountByPasswordResetCode(resetCode: UUID): Account?
}

