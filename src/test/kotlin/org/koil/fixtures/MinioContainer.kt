package org.koil.fixtures

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy

import org.testcontainers.utility.Base58
import java.time.Duration


class MinioContainer(image: String = "minio/minio:edge") : GenericContainer<MinioContainer>(image) {

    companion object {
        const val minioAccessKey = "minio"
        const val minioSecretKey = "password"
    }

    private val defaultPort = 9000

    init {
        withNetworkAliases("minio-" + Base58.randomString(6))
        addExposedPort(defaultPort)
        withEnv("MINIO_ACCESS_KEY", minioAccessKey)
        withEnv("MINIO_SECRET_KEY", minioSecretKey)
        withCommand("server", "/data")
        setWaitStrategy(
            HttpWaitStrategy()
                .forPort(defaultPort)
                .forPath("/minio/health/ready")
                .withStartupTimeout(Duration.ofMinutes(2))
        )
    }

    fun getEndpoint(): String {
        return "http://${containerIpAddress}:${getMappedPort(defaultPort)}"
    }
}

