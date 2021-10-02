package org.koil.user

data class NoAccountFoundUnexpectedlyException(val accountId: Long) :
    RuntimeException("Could not find account with ID $accountId")
