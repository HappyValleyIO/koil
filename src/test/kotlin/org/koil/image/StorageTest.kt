package org.koil.image

import assertk.assertThat
import assertk.assertions.isNotEmpty
import com.amazonaws.regions.Regions
import org.junit.jupiter.api.Test
import org.koil.config.BeanConfig
import org.koil.fixtures.MinioContainer
import java.util.*

class StorageTest {
    private val minio = MinioContainer().also { it.start() }

    val s3Client = BeanConfig().s3Client(
        s3AccessKey = MinioContainer.minioAccessKey,
        s3SecretKey = MinioContainer.minioSecretKey,
        s3Endpoint = minio.getEndpoint(),
        s3Region = Regions.US_EAST_1.name
    )

    val storage = S3Storage(s3Client, "test-bucket")
    val example = this::class.java.getResourceAsStream("/img/large.jpg")!!

    @Test
    internal fun `can upload and retrieve a file`() {
        val id = UUID.randomUUID()

        storage.saveObject(id, example, "image/jpeg")

        val link = storage.getPresignedObjectUrl(id)

        assertThat(link).isNotEmpty()
    }
}
