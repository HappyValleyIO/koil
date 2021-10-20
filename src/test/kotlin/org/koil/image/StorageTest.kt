package org.koil.image

import assertk.assertThat
import assertk.assertions.isNotEmpty
import org.junit.jupiter.api.Test
import org.koil.config.BeanConfig
import org.koil.fixtures.MinioContainer
import java.util.*

class StorageTest {
    private val minio = MinioContainer().also { it.start() }

    val s3Client = BeanConfig().s3Client(
        s3AccessKey = MinioContainer.minioAccessKey,
        s3SecretKey = MinioContainer.minioSecretKey,
        s3Endpoint = minio.getEndpoint()
    )

    val storage = S3Storage(s3Client, "test-bucket")
    val example = this::class.java.getResourceAsStream("/img/large.jpg")!!

    @Test
    internal fun `can upload and retrieve a file`() {
        val id = UUID.randomUUID()

        storage.saveImage(id, example, "image/jpeg")

        val link = storage.getPresignedImageUrl(id)

        assertThat(link).isNotEmpty()
    }
}
