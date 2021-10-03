package org.koil.user.verification

import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.peek
import org.koil.user.Account
import org.koil.user.AccountRepository
import org.koil.user.NoAccountFoundUnexpectedlyException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface AccountVerificationService {
    fun verifyAccount(accountId: Long, code: UUID): Result<Account, AccountVerificationViolations>
}

@Service
class DefaultAccountVerificationService(
    private val repository: AccountRepository,
    private val publisher: ApplicationEventPublisher
) : AccountVerificationService {
    @Transactional
    override fun verifyAccount(accountId: Long, code: UUID): Result<Account, AccountVerificationViolations> {
        return repository.findByIdOrNull(accountId)?.let { account ->
            account.verifyAccount(code).map { verified ->
                repository.save(verified)
            }.peek {
                publisher.publishEvent(AccountVerifiedEvent(it, this))
            }
        } ?: throw NoAccountFoundUnexpectedlyException(accountId)
    }
}
