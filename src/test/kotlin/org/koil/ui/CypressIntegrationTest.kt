package org.koil.ui

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.testcontainers.Testcontainers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.OutputFrame
import org.testcontainers.containers.output.Slf4jLogConsumer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CypressIntegrationTest {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(CypressIntegrationTest::class.java)
    }

    val testResult = AtomicBoolean(false)

    @LocalServerPort
    var port = 0

    private fun createCypressContainer(): KGenericContainer? {
        val result: KGenericContainer = KGenericContainer("cypress/included:6.2.0")
                .withCommand("run")
                .withLogConsumer(Slf4jLogConsumer(LOGGER))
                .withSharedMemorySize(1024L * 1024 * 1024 * 4) // Chrome chews through memory. This is a hacky way to stop it from dying

        result.withFileSystemBind("build/webapp", "/e2e")
        result.workingDirectory = "/e2e"
        result.addEnv("CYPRESS_baseUrl", "http://host.testcontainers.internal:$port")
        return result
    }

    @Test
    @Throws(InterruptedException::class)
    fun runCypressTests() {
        Testcontainers.exposeHostPorts(port)

        val countDownLatch = CountDownLatch(1)

        createCypressContainer()!!.use { container ->
            container.start()

            container.followOutput {
                when {
                    it.utf8String.contains("All specs passed") -> {
                        testResult.set(true)
                        countDownLatch.countDown()
                    }
                    it.utf8String.contains("tests failed") -> {
                        testResult.set(false)
                        countDownLatch.countDown()
                    }
                    it.type == OutputFrame.OutputType.END -> {
                        it.utf8String
                        countDownLatch.countDown()
                    }
                }
            }
            countDownLatch.await(5, TimeUnit.MINUTES)
        }

        assertTrue(testResult.get())
    }
}
