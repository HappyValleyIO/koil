package org.koil.image

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

interface Storage {
    fun saveObject(id: UUID, file: InputStream, contentType: String)
    fun getPresignedObjectUrl(id: UUID): String
}

class S3Storage(
    private val s3Client: AmazonS3,
    private val bucketName: String
) : Storage {
    companion object {
        val LOG: Logger = LoggerFactory.getLogger(S3Storage::class.java)!!
    }

    init {
        createBucketIfNotExists()
    }

    override fun saveObject(id: UUID, file: InputStream, contentType: String) {
        val metadata = ObjectMetadata()
        metadata.contentLength = file.available().toLong()
        metadata.contentType = contentType

        val request = PutObjectRequest(bucketName, id.toString(), file, metadata)

        s3Client.putObject(request)
    }

    override fun getPresignedObjectUrl(id: UUID): String {
        val request = GeneratePresignedUrlRequest(bucketName, id.toString(), HttpMethod.GET)
            .withExpiration(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))

        return s3Client.generatePresignedUrl(request).toExternalForm()
    }

    private fun createBucketIfNotExists() {
        try {
            if (!s3Client.doesBucketExistV2(bucketName)) {
                LOG.info("Creating bucket [$bucketName]")
                s3Client.createBucket(bucketName)
            } else {
                LOG.info("Bucket [$bucketName] already exists")
            }
        } catch (e: AmazonS3Exception) {
            if (e.errorCode != "BucketAlreadyOwnedByYou") {
                throw e
            }
        }
    }
}
