package org.koil.admin

import org.koil.user.Account

sealed class AdminAccountUpdateResult {
    data class AccountUpdateSuccess(val account: Account) : AdminAccountUpdateResult()
    data class EmailAlreadyTaken(val account: Account) : AdminAccountUpdateResult()
    object CouldNotFindAccount : AdminAccountUpdateResult()
}
