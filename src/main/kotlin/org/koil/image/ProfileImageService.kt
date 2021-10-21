package org.koil.image

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface ProfileImageService {
    fun saveProfileImage(accountId: Long, file: MultipartFile): ProfileImage

    fun getProfileImageUrl(accountId: Long): String
}

@Service
class PersistedProfileImageService(
    private val storage: Storage,
    private val repository: ProfileImageRepository,
    private val imageNormalizer: ImageNormalizer
) : ProfileImageService {
    @Transactional
    override fun saveProfileImage(accountId: Long, file: MultipartFile): ProfileImage {
        val profileImage = repository.findProfileImageByAccountId(accountId)
            ?: ProfileImage.createForAccount(accountId).let(repository::save)

        return profileImage.also {
            storage.saveObject(
                profileImage.publicId,
                imageNormalizer.normalize(file.inputStream),
                file.contentType ?: ""
            )
        }
    }

    override fun getProfileImageUrl(accountId: Long): String =
        repository.findProfileImageByAccountId(accountId)?.let { image ->
            storage.getPresignedObjectUrl(image.publicId)
        } ?: "/img/placeholder-user-image.png"
}
