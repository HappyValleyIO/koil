package org.koil.config

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.koil.auth.UserRole
import org.koil.image.S3Storage
import org.koil.image.Storage
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter


@Configuration
@EnableAsync
class BeanConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun switchUserFilter(userDetailsService: UserDetailsService): SwitchUserFilter {
        return SwitchUserFilter().apply {
            setUserDetailsService(userDetailsService)
            setTargetUrl("/dashboard")
            setSwitchUserUrl("/admin/impersonation")
            setExitUserUrl("/admin/impersonation/logout")
            setSwitchAuthorityRole(UserRole.ADMIN_IMPERSONATING_USER.name)
        }
    }

    @Bean
    @Profile("!test")
    fun s3Client(
        @Value("\${cloud.aws.credentials.access-key}") s3AccessKey: String,
        @Value("\${cloud.aws.credentials.secret-key}") s3SecretKey: String,
        @Value("\${s3.endpoint}") s3Endpoint: String,
        @Value("\${s3.region}") s3Region: String,
    ): AmazonS3 {
        val credentials: AWSCredentials = BasicAWSCredentials(s3AccessKey, s3SecretKey)
        val clientConfiguration = ClientConfiguration()
        clientConfiguration.signerOverride = "AWSS3V4SignerType"

        return AmazonS3ClientBuilder
            .standard()
            .withEndpointConfiguration(EndpointConfiguration(s3Endpoint, s3Region))
            .withPathStyleAccessEnabled(true)
            .withClientConfiguration(clientConfiguration)
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .build()
    }

    @Bean
    @Profile("!test")
    fun storage(
        s3Client: AmazonS3,
        @Value("\${s3.bucket-name}") bucketName: String
    ): Storage = S3Storage(s3Client, bucketName)
}
