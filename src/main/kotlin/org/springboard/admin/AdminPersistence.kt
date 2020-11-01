package org.springboard.admin

import org.springboard.user.Account
import org.springboard.user.InternalUserPersistence
import org.springframework.stereotype.Component

interface IAdminPersistence {
    fun getAllAccounts(): List<Account>
}

@Component
class AdminPersistence(private val userPersistence: InternalUserPersistence) : IAdminPersistence {
    override fun getAllAccounts(): List<Account> {
        return userPersistence.getUsers()
                .map {
                    it.toAccount()
                }
    }
}
