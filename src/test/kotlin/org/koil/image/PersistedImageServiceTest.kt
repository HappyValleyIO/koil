package org.koil.image

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockMultipartFile

class PersistedProfileImageServiceTest : BaseIntegrationTest() {

    @Autowired
    lateinit var profileImageService: ProfileImageService

    val example = MockMultipartFile(
        "large.jpg",
        "large.jpg",
        "IMAGE/JPEG",
        this::class.java.getResourceAsStream("/img/large.jpg")!!
    )
    val alternative = MockMultipartFile(
        "large.png",
        "large.png",
        "IMAGE/PNG",
        this::class.java.getResourceAsStream("/img/large.png")!!
    )
    val exampleScaled = this::class.java.getResourceAsStream("/img/scaled-jpg.png")!!

    @Test
    internal fun `GIVEN a user hasn't set their image yet WHEN setting image THEN do so successfully`() {
        withTestAccount { user ->
            val result = profileImageService.saveImageForUser(user.accountId!!, example)

            assertThat(result.id).isNotNull()

            val retrieved = profileImageService.getImageUrlForUser(user.accountId!!)

            assertThat(retrieved)
                .isEqualTo(result.publicId.toString())
        }
    }

    @Test
    internal fun `GIVEN a user has set their image WHEN setting image again THEN overwrite the image and record updated`() {
        withTestAccount { user ->
            // Set for the first time
            profileImageService.saveImageForUser(user.accountId!!, alternative)

            // Update the image
            val result = profileImageService.saveImageForUser(user.accountId!!, example)

            assertThat(result.id).isNotNull()

            val retrieved = profileImageService.getImageUrlForUser(user.accountId!!)

            assertThat(retrieved)
                .isEqualTo(result.publicId.toString())
        }
    }

    @Test
    internal fun `GIVEN a user hasn't set their image WHEN retrieving the image THEN return default image`() {
        withTestAccount { user ->
            val retrieved = profileImageService.getImageUrlForUser(user.accountId!!)

            assertThat(retrieved)
                .isEqualTo("/img/placeholder-user-image.png")
        }
    }
}
