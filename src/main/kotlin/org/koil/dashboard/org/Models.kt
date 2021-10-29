package org.koil.dashboard.org

import org.koil.user.Account

sealed class OrgAccountUpdateResult {
    data class AccountUpdateSuccess(val account: Account) : OrgAccountUpdateResult()
    data class EmailAlreadyTaken(val account: Account) : OrgAccountUpdateResult()
    object CouldNotFindAccount : OrgAccountUpdateResult()
}
