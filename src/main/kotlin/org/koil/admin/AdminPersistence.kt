package org.koil.admin

import org.koil.user.Account
import org.koil.user.AccountRepository
import org.springframework.stereotype.Component

interface IAdminPersistence {
    fun getAllAccounts(): List<Account>
}

@Component
class AdminPersistence(private val accountRepository: AccountRepository) : IAdminPersistence {
    override fun getAllAccounts(): List<Account> {
        return accountRepository.findAll().toList()
    }
}
