package org.koil.image

import org.springframework.data.repository.CrudRepository
import java.util.*

interface ProfileImageRepository : CrudRepository<ProfileImage, UUID> {
    fun findProfileImageByAccountId(accountId: Long): ProfileImage?
}
