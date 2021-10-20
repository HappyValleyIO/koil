package org.koil.image

import org.springframework.data.repository.CrudRepository
import java.util.*

interface AccountImageRepository : CrudRepository<AccountImage, UUID> {
    fun findAccountImageByAccountId(accountId: Long): AccountImage?
}
