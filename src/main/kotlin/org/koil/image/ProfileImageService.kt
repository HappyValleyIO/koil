package org.koil.image

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface ProfileImageService {
    fun saveImageForUser(accountId: Long, file: MultipartFile): AccountImage

    fun getImageUrlForUser(accountId: Long): String
}

@Service
class PersistedProfileImageService(
    private val storage: Storage,
    private val repository: AccountImageRepository,
    private val imageResizer: ImageResizer
) : ProfileImageService {
    @Transactional
    override fun saveImageForUser(accountId: Long, file: MultipartFile): AccountImage {
        val accountImage = repository.findAccountImageByAccountId(accountId)
            ?: AccountImage.createForAccount(accountId).let(repository::save)

        return accountImage.also {
            storage.saveImage(accountImage.publicId, imageResizer.resize(file.inputStream), file.contentType ?: "")
        }
    }

    override fun getImageUrlForUser(accountId: Long): String =
        repository.findAccountImageByAccountId(accountId)?.let { image ->
            storage.getPresignedImageUrl(image.publicId)
        } ?: "/img/placeholder-user-image.png"
}
