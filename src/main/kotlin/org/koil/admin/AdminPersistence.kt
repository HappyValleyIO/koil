package org.koil.admin

import org.koil.user.Account
import org.koil.user.AccountRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

interface IAdminPersistence {
    fun getAllAccounts(pageable: Pageable): Page<Account>

    fun findById(accountId: Long): Account?
}

@Component
class AdminPersistence(private val accountRepository: AccountRepository) : IAdminPersistence {
    override fun getAllAccounts(pageable: Pageable): Page<Account> {
        return accountRepository.findAll(pageable)
    }

    override fun findById(accountId: Long): Account? {
        return accountRepository.findByIdOrNull(accountId)
    }
}
